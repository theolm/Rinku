package dev.theolm.rinku.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.theolm.rinku.Rinku
import dev.theolm.rinku.compose.ext.Rinku as RinkuHost
import dev.theolm.rinku.models.DeepLinkFilter
import dev.theolm.rinku.models.DeepLinkMapper
import kotlinx.browser.document
import kotlinx.browser.window

/** History writes belong to this sample application, never to the Rinku adapter. */
@Composable
fun WebDemo(content: @Composable () -> Unit) {
    val origin = window.location.origin
    // Set this meta tag when deploying under a prefix, for example /rinku/.
    val basePath = remember {
        document.querySelector("meta[name='rinku-base-path']")?.getAttribute("content") ?: "/"
    }
    val filter = remember(origin) {
        object : DeepLinkFilter {
            override fun isValid(url: String) = url.startsWith("$origin/")
        }
    }
    val mapper = remember(origin, basePath) {
        object : DeepLinkMapper {
            override fun map(url: String) = if (url.startsWith("$origin$basePath")) {
                "$origin/" + url.removePrefix("$origin$basePath")
            } else {
                url
            }
        }
    }
    RinkuHost(deepLinkFilter = filter, deepLinkMapper = mapper) {
        Column(Modifier.fillMaxSize()) {
            Text("History is controlled by the sample; Rinku receives initial URLs and Back/Forward.")
            Row {
                Button(onClick = { window.history.back() }) { Text("Browser back") }
                Button(onClick = { window.history.forward() }) { Text("Browser forward") }
            }
            Row {
                listOf("", "second", "second/third").forEach { route ->
                    Button(onClick = {
                        window.history.pushState(null, "", "$basePath$route?source=web")
                        // pushState does not fire popstate. Notify shared navigation explicitly,
                        // applying the same filter/mapper as the browser adapter, exactly once.
                        Rinku.handleDeepLink(window.location.href, applyFilter = true, applyMapper = true)
                    }) { Text(route.ifEmpty { "Home" }) }
                }
            }
            Column(Modifier.weight(1f)) { content() }
        }
    }
}
