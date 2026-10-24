package dev.theolm.rinku.desktop

import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DesktopInstanceTest {
    @Test
    fun `secondary forwards links and primary can restart after close`() = withState { root ->
        val received = mutableListOf<String>()
        val errors = mutableListOf<Exception>()
        DesktopInstance("test", received::add, errors::add, root).use { primary ->
            assertTrue(primary.start(emptyList()))
            DesktopInstance("test", {}, errors::add, root).use { secondary ->
                assertFalse(secondary.start(listOf("rinku://first", "rinku://first", "rinku://second")))
            }
            assertEquals(listOf("rinku://first", "rinku://first", "rinku://second"), received)
        }
        DesktopInstance("test", {}, errors::add, root).use { restarted ->
            assertTrue(restarted.start(emptyList()))
        }
        assertTrue(errors.isEmpty(), errors.toString())
    }

    @Test
    fun `different application ids have independent primaries`() = withState { root ->
        DesktopInstance("first", {}, {}, root).use { first ->
            DesktopInstance("second", {}, {}, root).use { second ->
                assertTrue(first.start(emptyList()))
                assertTrue(second.start(emptyList()))
            }
        }
    }

    @Test
    fun `primary initialization runs once before accepting forwarded events`() = withState { root ->
        val received = mutableListOf<String>()
        DesktopInstance("test", received::add, {}, root).use { primary ->
            assertTrue(primary.start(emptyList()) { received += "startup" })
            DesktopInstance("test", {}, {}, root).use { secondary ->
                assertFalse(secondary.start(listOf("rinku://later")) { error("Not the primary") })
            }
            assertEquals(listOf("startup", "rinku://later"), received)
        }
    }

    @Test
    fun `oversized outgoing requests are rejected`() = withState { root ->
        DesktopInstance("test", {}, {}, root).use { instance ->
            assertFailsWith<IllegalArgumentException> {
                instance.start(List(65) { "rinku://first" })
            }
        }
    }

    @Test
    fun `endpoint and lock are owner-only on POSIX filesystems`() = withState { root ->
        if (Files.getFileAttributeView(root, java.nio.file.attribute.PosixFileAttributeView::class.java) != null) {
            DesktopInstance("test", {}, {}, root).use { instance ->
                instance.start(emptyList())
                Files.walk(root).use { paths ->
                    paths.forEach { path ->
                        val permissions = Files.getPosixFilePermissions(path)
                        assertTrue(permissions.all { it.name.startsWith("OWNER_") })
                    }
                }
            }
        }
    }

    @Test
    fun `invalid authenticated request does not stop the primary`() = withState { root ->
        val received = mutableListOf<String>()
        val rejected = CountDownLatch(1)
        DesktopInstance("test", received::add, { rejected.countDown() }, root).use { primary ->
            primary.start(emptyList())
            val endpoint = Files.walk(root).use { paths ->
                paths.filter { it.fileName.toString() == "instance" }.findFirst().orElseThrow()
            }
            val (port, token) = Files.readString(endpoint).split('\n')
            Socket("127.0.0.1", port.toInt()).use { socket ->
                socket.soTimeout = 2_000
                val output = DataOutputStream(socket.getOutputStream())
                output.writeUTF(token)
                output.flush()
                assertTrue(DataInputStream(socket.getInputStream()).readBoolean())
                output.writeInt(Int.MAX_VALUE)
                output.flush()
                assertTrue(rejected.await(5, TimeUnit.SECONDS))
            }
            DesktopInstance("test", {}, {}, root).use { secondary ->
                assertFalse(secondary.start(listOf("rinku://valid")))
            }
            assertEquals(listOf("rinku://valid"), received)
        }
    }

    @Test
    fun `concurrent startup elects one primary`() = withState { root ->
        val ready = CountDownLatch(1)
        val results = java.util.Collections.synchronizedList(mutableListOf<Boolean>())
        val errors = java.util.Collections.synchronizedList(mutableListOf<Exception>())
        val instances = List(4) { DesktopInstance("test", {}, errors::add, root) }
        try {
            val threads = instances.map { instance ->
                thread {
                    ready.await()
                    try {
                        results += instance.start(emptyList())
                    } catch (error: Exception) {
                        errors += error
                    }
                }
            }
            ready.countDown()
            threads.forEach { it.join(10_000) }
            assertTrue(threads.none { it.isAlive })
            assertTrue(errors.isEmpty(), errors.toString())
            assertEquals(1, results.count { it })
            assertEquals(3, results.count { !it })
        } finally {
            instances.forEach { it.close() }
        }
    }

    @Test
    fun `unauthenticated connection cannot deliver links`() = withState { root ->
        val received = mutableListOf<String>()
        DesktopInstance("test", received::add, {}, root).use { primary ->
            primary.start(emptyList())
            val endpoint = Files.walk(root).use { paths ->
                paths.filter { it.fileName.toString() == "instance" }.findFirst().orElseThrow()
            }
            val port = Files.readString(endpoint).substringBefore('\n').toInt()
            Socket("127.0.0.1", port).use { socket ->
                socket.soTimeout = 2_000
                DataOutputStream(socket.getOutputStream()).apply { writeUTF("wrong-token"); flush() }
                assertFalse(DataInputStream(socket.getInputStream()).readBoolean())
            }
            assertTrue(received.isEmpty())
            DesktopInstance("test", {}, {}, root).use { secondary ->
                assertFalse(secondary.start(listOf("rinku://valid")))
            }
            assertEquals(listOf("rinku://valid"), received)
        }
    }

    @Test
    fun `separate process forwards and abrupt exit releases the lock`() = withState { root ->
        val output = root.resolve("received.txt")
        val process = ProcessBuilder(
            Path.of(System.getProperty("java.home"), "bin", "java").toString(),
            "-cp", System.getProperty("rinku.test.classpath"),
            DesktopProcess::class.java.name, root.toString(), output.toString()
        ).redirectErrorStream(true).start()
        try {
            assertEquals("READY", process.inputStream.bufferedReader().readLine())
            DesktopInstance("process-test", {}, {}, root).use { secondary ->
                assertFalse(secondary.start(listOf("rinku://from-another-process")))
            }
            assertEquals("rinku://from-another-process", Files.readString(output))
        } finally {
            process.destroyForcibly()
            assertTrue(process.waitFor(10, TimeUnit.SECONDS))
        }
        DesktopInstance("process-test", {}, {}, root).use { restarted ->
            assertTrue(restarted.start(emptyList()))
        }
    }

    private fun withState(block: (Path) -> Unit) {
        val root = Files.createTempDirectory("rinku-ipc-test")
        try {
            block(root)
        } finally {
            Files.walk(root).use { paths ->
                paths.sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) }
            }
        }
    }
}

/** Child JVM used to exercise real process boundaries and operating-system lock recovery. */
object DesktopProcess {
    @JvmStatic
    fun main(args: Array<String>) {
        val instance = DesktopInstance(
            "process-test", { Files.writeString(Path.of(args[1]), it) },
            { it.printStackTrace() }, Path.of(args[0])
        )
        check(instance.start(emptyList()))
        println("READY")
        CountDownLatch(1).await()
    }
}
