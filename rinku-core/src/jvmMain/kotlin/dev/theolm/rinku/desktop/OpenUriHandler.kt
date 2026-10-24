package dev.theolm.rinku.desktop

import java.awt.Desktop

/** AWT supplies macOS OpenURI events for packaged applications, including cold starts. */
internal object OpenUriHandler {
    fun install(onUri: (String) -> Unit): AutoCloseable? {
        if (!Desktop.isDesktopSupported()) return null
        val desktop = Desktop.getDesktop()
        if (!desktop.isSupported(Desktop.Action.APP_OPEN_URI)) return null
        desktop.setOpenURIHandler { onUri(it.uri.toString()) }
        return AutoCloseable { desktop.setOpenURIHandler(null) }
    }
}
