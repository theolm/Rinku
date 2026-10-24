package dev.theolm.rinku.desktop

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.READ
import java.nio.file.StandardOpenOption.WRITE
import java.nio.file.attribute.AclEntry
import java.nio.file.attribute.AclEntryPermission
import java.nio.file.attribute.AclEntryType
import java.nio.file.attribute.AclFileAttributeView
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermissions
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.HexFormat
import kotlin.concurrent.thread

/** Per-user single-instance transport. The lock is authoritative; endpoint data can be stale. */
internal class DesktopInstance(
    applicationId: String,
    private val onLink: (String) -> Unit,
    private val onError: (Exception) -> Unit,
    private val root: Path = Path.of(System.getProperty("user.home"), ".rinku"),
) : AutoCloseable {
    private val key: String
    private var channel: FileChannel? = null
    private var lock: FileLock? = null
    private var server: ServerSocket? = null
    @Volatile
    private var client: Socket? = null
    @Volatile
    private var closed = false

    init {
        require(applicationId.isNotBlank()) { "applicationId must not be blank" }
        key = HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(applicationId.toByteArray(Charsets.UTF_8))
        )
    }

    /** Returns true for the primary, false only after successful forwarding. */
    fun start(links: List<String>, onPrimary: () -> Unit = {}): Boolean {
        check(channel == null && !closed) { "Desktop instance already started or closed" }
        require(links.size <= MAX_LINKS && links.all { it.length <= MAX_URL_LENGTH })
        secureDirectory(root)
        val directory = root.resolve(key)
        secureDirectory(directory)
        val endpoint = directory.resolve("instance")
        val lockPath = directory.resolve("instance.lock")
        val file = FileChannel.open(lockPath, CREATE, READ, WRITE, NOFOLLOW_LINKS)
        channel = file
        restrictAccess(lockPath, directory = false)
        val deadline = System.nanoTime() + STARTUP_TIMEOUT_MS * NANOS_PER_MILLISECOND
        while (System.nanoTime() < deadline) {
            val acquired = try {
                file.tryLock()
            } catch (_: OverlappingFileLockException) {
                null
            }
            if (acquired != null) {
                lock = acquired
                onPrimary()
                listen(endpoint)
                return true
            }
            if (forward(endpoint, links)) {
                file.close()
                channel = null
                return false
            }
            Thread.sleep(RETRY_DELAY_MS)
        }
        throw IOException("Timed out connecting to the primary Rinku desktop instance")
    }

    private fun listen(endpoint: Path) {
        val listener = ServerSocket()
        server = listener
        listener.bind(InetSocketAddress(LOOPBACK, 0), CONNECTION_BACKLOG)
        val token = HexFormat.of().formatHex(ByteArray(TOKEN_BYTES).also { SecureRandom().nextBytes(it) })
        val bytes = "${listener.localPort}\n$token".toByteArray(Charsets.UTF_8)
        // Keep discovery separate: Windows file locks also prohibit reads of the locked region.
        FileChannel.open(endpoint, CREATE, WRITE, NOFOLLOW_LINKS).use { file ->
            restrictAccess(endpoint, directory = false)
            file.truncate(0)
            val buffer = ByteBuffer.wrap(bytes)
            while (buffer.hasRemaining()) file.write(buffer)
            file.force(true)
        }
        thread(name = "rinku-desktop-ipc", isDaemon = true) {
            while (!closed) {
                try {
                    listener.accept().use { socket ->
                        client = socket
                        if (closed) return@use
                        receive(socket, token)
                    }
                } catch (error: Exception) {
                    if (!closed) onError(error)
                } finally {
                    client = null
                }
            }
        }
    }

    private fun receive(socket: Socket, token: String) {
        socket.soTimeout = SOCKET_TIMEOUT_MS
        val input = DataInputStream(socket.getInputStream())
        val output = DataOutputStream(socket.getOutputStream())
        val authenticated = MessageDigest.isEqual(
            input.readUTF().toByteArray(Charsets.UTF_8), token.toByteArray(Charsets.UTF_8)
        )
        output.writeBoolean(authenticated)
        output.flush()
        if (!authenticated) return
        val count = input.readInt()
        require(count in 0..MAX_LINKS) { "Invalid IPC link count" }
        val links = List(count) {
            input.readUTF().also { require(it.length <= MAX_URL_LENGTH) { "IPC URL too long" } }
        }
        links.forEach(onLink)
        output.writeBoolean(true)
        output.flush()
    }

    private fun forward(endpoint: Path, links: List<String>): Boolean {
        val data = try {
            if (Files.size(endpoint) > MAX_ENDPOINT_BYTES) return false
            Files.readString(endpoint).split('\n')
        } catch (_: IOException) {
            return false
        }
        val port = data.firstOrNull()?.toIntOrNull()?.takeIf { it in 1..MAX_PORT } ?: return false
        val token = data.getOrNull(1)?.takeIf { it.length == TOKEN_BYTES * 2 } ?: return false
        Socket().use { socket ->
            try {
                socket.connect(InetSocketAddress(LOOPBACK, port), SOCKET_TIMEOUT_MS)
            } catch (_: IOException) {
                return false
            }
            socket.soTimeout = SOCKET_TIMEOUT_MS
            val input = DataInputStream(socket.getInputStream())
            val output = DataOutputStream(socket.getOutputStream())
            try {
                output.writeUTF(token)
                output.flush()
                if (!input.readBoolean()) return false
            } catch (_: IOException) {
                return false
            }
            // Never retry after sending a payload: a lost acknowledgement must not duplicate links.
            output.writeInt(links.size)
            links.forEach(output::writeUTF)
            output.flush()
            check(input.readBoolean()) { "Primary instance rejected the links" }
            return true
        }
    }

    override fun close() {
        closed = true
        try {
            server?.close()
            client?.close()
        } finally {
            try {
                lock?.release()
            } finally {
                channel?.close()
            }
        }
        // Keep the file: unlinking a locked inode permits two different lock owners.
        lock = null
        channel = null
    }

    private fun secureDirectory(path: Path) {
        if (!Files.exists(path, NOFOLLOW_LINKS)) {
            try {
                Files.createDirectory(path)
            } catch (_: java.nio.file.FileAlreadyExistsException) {
                // Another process may have created the directory during startup.
            }
        }
        require(Files.isDirectory(path, NOFOLLOW_LINKS)) { "Unsafe Rinku state directory: $path" }
        restrictAccess(path, directory = true)
    }

    private fun restrictAccess(path: Path, directory: Boolean) {
        require(!Files.isSymbolicLink(path)) { "Symbolic links are not allowed for Rinku state" }
        val user = path.fileSystem.userPrincipalLookupService
            .lookupPrincipalByName(System.getProperty("user.name"))
        val posix = Files.getFileAttributeView(path, PosixFileAttributeView::class.java, NOFOLLOW_LINKS)
        if (posix != null) {
            check(Files.getOwner(path, NOFOLLOW_LINKS) == user) { "Rinku state must belong to the current user" }
            posix.setPermissions(PosixFilePermissions.fromString(if (directory) "rwx------" else "rw-------"))
        } else {
            val acl = checkNotNull(Files.getFileAttributeView(path, AclFileAttributeView::class.java)) {
                "Rinku requires a filesystem with owner-only permissions"
            }
            acl.acl = listOf(
                AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(user)
                    .setPermissions(*AclEntryPermission.entries.toTypedArray()).build()
            )
        }
    }

    companion object {
        const val MAX_LINKS = 64
        const val MAX_URL_LENGTH = 16_384
        private const val SOCKET_TIMEOUT_MS = 1_500
        private const val STARTUP_TIMEOUT_MS = 5_000L
        private const val RETRY_DELAY_MS = 50L
        private const val NANOS_PER_MILLISECOND = 1_000_000L
        private const val TOKEN_BYTES = 32
        private const val MAX_ENDPOINT_BYTES = 128
        private const val MAX_PORT = 65_535
        private const val CONNECTION_BACKLOG = 8
        private val LOOPBACK = InetAddress.getByName("127.0.0.1")
    }
}
