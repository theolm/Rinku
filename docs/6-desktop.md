# Desktop support

Rinku supports JVM 17+ applications on macOS, Windows, and Linux, with or without Compose Desktop.
The platform adapter receives URLs, applies your filter and mapper, and delivers `DeepLink` objects
through the same `DeepLinkListener` / `listenForDeepLinks` APIs used on mobile. Navigation stays in
your application. The existing `jvm()` target is used; Kotlin/Native desktop targets are not included.

## Initialization

Add `dev.theolm:rinku:<latest_version>` to `commonMain` (or your JVM application). Add
`dev.theolm:rinku-compose-ext:<latest_version>` if using Compose. No separate desktop artifact is needed.

Initialize **once, before creating any windows**, not inside a composable:

```kotlin
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.theolm.rinku.RinkuDesktop

fun main(args: Array<String>) {
    RinkuDesktop(
        applicationId = "com.example.myapp",
        schemes = setOf("myapp"),
        // deepLinkFilter = MyFilter(),
        // deepLinkMapper = MyMapper(),
        onError = { error -> error.printStackTrace() },
    ).use { rinku ->
        if (rinku.start(args) == RinkuDesktop.StartResult.FORWARDED) return
        application(exitProcessOnExit = false) {
            Window(onCloseRequest = ::exitApplication, title = "My app") {
                App() // Keep DeepLinkListener in your shared UI.
            }
        }
    }
}
```

For non-Compose applications, keep the adapter alive until your event loop ends, call `close()` on
shutdown, and collect `listenForDeepLinks` in an application-owned coroutine scope. Rinku includes
`kotlinx-coroutines-swing` on JVM so existing `Rinku.handleDeepLink` calls have a Main dispatcher
even without Compose. Desktop URLs are queued synchronously; listeners execute in the collector's
coroutine context. Install your listener on the appropriate UI dispatcher before navigating.

`start()` returns `PRIMARY` for the process that should create the UI, or `FORWARDED` after a
secondary process contacts the primary. Rinku never calls `exitProcess`. Startup/IPC failures throw;
do not respond to them by opening an independent second instance. Repeated `start()` calls on the
same adapter return its original result without reprocessing arguments. A closed adapter cannot restart.

Only absolute, syntactically valid URIs matching the configured schemes are accepted. Pass each URL
as a separate argument, without an extra `--url=` prefix. Percent-encode spaces and JSON values.
Other arguments and unsupported schemes are ignored. The filter runs before the mapper; both run
on the receiving thread and should be fast and thread-safe. `onDeepLinkReceived(url)` is also
available for applications integrating their own native events. Do not install a competing AWT
OpenURI handler when using Rinku's automatic handler.

## Startup delivery and limits

- Desktop links wait in an in-memory FIFO until a shared listener is installed. Repeated identical
  links remain separate events. Use one logical listener: delivery is single-consumer, not broadcast.
- Up to **64 pending desktop links** are retained. When full, the newest link is rejected and
  `onError` is called. Events are not persisted across process exits.
- Each input URL is limited to **16,384 UTF-16 code units**; longer inputs are ignored. At most
  64 accepted URLs may be supplied in one startup/IPC request.
- Existing mobile/internal `handleDeepLink` calls retain their single-pending-value behavior; they
  are not converted to FIFO delivery. If mixed with desktop links, that pending value is consumed first.
- `onError` reports mapping, parsing, queue overflow, and incoming IPC errors. Keep the callback
  non-throwing. IPC acknowledgement confirms processing by the adapter, not successful navigation
  or persistence; filtered/invalid links are not navigated. A lost acknowledgement raises an error
  rather than automatically resending a potentially delivered link.

## Single-instance behavior

Use a stable, unique `applicationId` (different IDs for different apps or intentionally separate
development/release instances). Instance election uses an OS file lock. Authenticated loopback IPC
forwards secondary startup URLs to the primary, including when no UI listener exists yet.

Per-user endpoint data is stored under `~/.rinku/<SHA-256-of-applicationId>/instance` (with a separate
`instance.lock` for election), with owner-only
POSIX permissions or Windows ACLs. A fresh random token is generated for each primary. The state
directory must be on a local filesystem supporting locks and permissions; sandboxed deployments
must allow that directory and loopback sockets. Startup discovery retries for approximately five
seconds; socket operations have 1.5-second timeouts. The transport is not intended as a security
boundary against other code running as the same OS user.

`close()` releases sockets, the file lock and the native URL handler. OS process termination also
releases locks; stale endpoint data is replaced by the next primary. Do not delete the lock file
while an instance is running. Single-instance delivery does not automatically focus or restore
your application window.

## Registering a URL scheme

As on mobile, registration is the responsibility of the application package/installer, not Rinku.
This release covers custom schemes, not verified HTTPS app/universal links. Validate incoming
paths and parameters as untrusted input; registering a scheme does not authenticate the sender.

### macOS

Rinku installs `Desktop.setOpenURIHandler` when `APP_OPEN_URI` is supported. macOS delivers opening
events to the packaged application, including startup events. Declare the scheme in your bundle's
`Info.plist`; with Compose Desktop:

```kotlin
compose.desktop.application.nativeDistributions.macOS {
    bundleID = "com.example.myapp"
    infoPlist {
        extraKeysRawXml = """
            <key>CFBundleURLTypes</key>
            <array><dict>
                <key>CFBundleTypeRole</key><string>Editor</string>
                <key>CFBundleURLName</key><string>com.example.myapp</string>
                <key>CFBundleURLSchemes</key><array><string>myapp</string></array>
            </dict></array>
        """.trimIndent()
    }
}
```

Install/launch the packaged `.app` so Launch Services recognizes it. Test with
`open 'myapp://example/second/third'` both before and after launching the app. Running with Gradle
does not register a protocol or prove native startup behavior. Where AWT OpenURI is unavailable,
the application must supply arguments or forward its own native events to `onDeepLinkReceived`.

### Windows

Register a per-user protocol in your installer. Example `.reg` content (replace the executable path):

```reg
Windows Registry Editor Version 5.00

[HKEY_CURRENT_USER\Software\Classes\myapp]
@="URL:My App Protocol"
"URL Protocol"=""

[HKEY_CURRENT_USER\Software\Classes\myapp\shell\open\command]
@="\"C:\\Users\\User\\AppData\\Local\\MyApp\\MyApp.exe\" \"%1\""
```

Quote both executable and URL. The packaged launcher must pass the URL unchanged to `main(args)`.
Compose's MSI packaging alone does not register this scheme; add registration/unregistration to
your installer. Test from PowerShell with `Start-Process 'myapp://example/second/third'`.

### Linux

Install a desktop entry under `~/.local/share/applications/myapp.desktop`, with an absolute launcher path:

```ini
[Desktop Entry]
Type=Application
Name=My App
Exec=/opt/myapp/bin/MyApp %u
Terminal=false
MimeType=x-scheme-handler/myapp;
```

Use a quoted executable path if it contains spaces. Register it using:

```shell
update-desktop-database ~/.local/share/applications
xdg-mime default myapp.desktop x-scheme-handler/myapp
xdg-open 'myapp://example/second/third'
```

Keep `%u` as a launcher argument; do not interpolate URLs into shell commands. Your installer must
also remove associations appropriately on uninstall. Compose's DEB alone does not provide this setup.

## Samples and verification

```shell
./gradlew :samples:voyager:desktopApp:run --args='rinku-voyager://example/second/third'
./gradlew :samples:decompose:desktopApp:run --args='rinku-decompose://example/second/third'
```

Both launchers reuse the existing shared navigation and screens. Run a sample a second time with
another URL: it should forward the link and exit without opening a second window. macOS bundle
scheme declarations are included; Windows/Linux registration remains an installer step as above.
Use `:samples:voyager:desktopApp:packageDistributionForCurrentOS` (or the Decompose equivalent) to
build a package on its target OS. Runtime modules include instrumentation/management modules
used by the samples' shared dependencies.

Before shipping, test each packaged application on its OS:

1. Open a registered link while the app is closed, then while it is already open.
2. Open repeated links and bursts before the first UI listener is ready.
3. Verify filters, mappers, encoded parameters, unsupported schemes and invalid URLs.
4. Start competing processes, terminate the primary abruptly, and reopen a link.
5. Confirm that a secondary exits, and registration is removed correctly on uninstall.

The desktop CI matrix runs shared/JVM tests, actual child-process IPC/lock-recovery tests and sample
compilation on all three operating systems. Native protocol registration and packaged OS-event
delivery still require the manual checks above.
