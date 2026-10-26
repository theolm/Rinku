package dev.theolm.rinku.compose.ext

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import dev.theolm.rinku.RinkuWeb
import dev.theolm.rinku.listenForDeepLinks
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.w3c.dom.PopStateEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RinkuWebCompositionTest {
    @Test
    fun recompositionDoesNotReplayAndDisposalRemovesAdapter() = runTest {
        val received = mutableListOf<String>()
        val clock = BroadcastFrameClock()
        val recomposer = Recomposer(coroutineContext + clock)
        val composition = Composition(EmptyApplier(), recomposer)
        val runner = backgroundScope.launch(clock) { recomposer.runRecomposeAndApplyChanges() }
        val revision = mutableStateOf(0)
        try {
            composition.setContent {
                val currentRevision = revision.value
                Rinku {
                    DeepLinkListener { received += "$currentRevision:${it.data}" }
                }
            }
            runCurrent()
            assertEquals(listOf("0:${window.location.href}"), received)
            revision.value++
            Snapshot.sendApplyNotifications()
            runCurrent()
            clock.sendFrame(1L)
            runCurrent()
            assertEquals(1, received.size)
            window.dispatchEvent(PopStateEvent("popstate"))
            runCurrent()
            assertEquals(listOf("0:${window.location.href}", "1:${window.location.href}"), received)
        } finally {
            composition.dispose()
            recomposer.cancel()
            runner.cancel()
        }
        val afterDisposal = mutableListOf<String>()
        backgroundScope.launch { listenForDeepLinks { afterDisposal += it.data } }
        runCurrent()
        window.dispatchEvent(PopStateEvent("popstate"))
        runCurrent()
        assertTrue(afterDisposal.isEmpty())
        // The wrapper released exclusive ownership, so manual initialization is possible again.
        val replacement = RinkuWeb(captureInitialUrl = false)
        try {
            replacement.start()
        } finally {
            replacement.close()
        }
    }

    @Test
    fun updatingErrorCallbackDoesNotRestartAdapterAndUsesLatestCallback() = runTest {
        val errors = mutableListOf<Int>()
        val clock = BroadcastFrameClock()
        val recomposer = Recomposer(coroutineContext + clock)
        val composition = Composition(EmptyApplier(), recomposer)
        val runner = backgroundScope.launch(clock) { recomposer.runRecomposeAndApplyChanges() }
        val revision = mutableStateOf(0)
        val filter = object : dev.theolm.rinku.models.DeepLinkFilter {
            override fun isValid(url: String): Boolean = error("Expected filter failure")
        }
        try {
            composition.setContent {
                val currentRevision = revision.value
                Rinku(deepLinkFilter = filter, onError = { errors += currentRevision }) {}
            }
            runCurrent()
            assertEquals(listOf(0), errors)
            revision.value++
            Snapshot.sendApplyNotifications()
            runCurrent()
            clock.sendFrame(1L)
            runCurrent()
            assertEquals(listOf(0), errors)
            window.dispatchEvent(PopStateEvent("popstate"))
            assertEquals(listOf(0, 1), errors)
        } finally {
            composition.dispose()
            recomposer.cancel()
            runner.cancel()
        }
    }

    @Test
    fun changingConfigurationReplacesAdapterAndHonorsInitialCapture() = runTest {
        val received = mutableListOf<String>()
        val clock = BroadcastFrameClock()
        val recomposer = Recomposer(coroutineContext + clock)
        val composition = Composition(EmptyApplier(), recomposer)
        val runner = backgroundScope.launch(clock) { recomposer.runRecomposeAndApplyChanges() }
        val capture = mutableStateOf(false)
        try {
            composition.setContent {
                Rinku(captureInitialUrl = capture.value) {
                    DeepLinkListener { received += it.data }
                }
            }
            runCurrent()
            assertTrue(received.isEmpty())
            capture.value = true
            Snapshot.sendApplyNotifications()
            runCurrent()
            clock.sendFrame(1L)
            runCurrent()
            assertEquals(listOf(window.location.href), received)
            window.dispatchEvent(PopStateEvent("popstate"))
            runCurrent()
            assertEquals(listOf(window.location.href, window.location.href), received)
        } finally {
            composition.dispose()
            recomposer.cancel()
            runner.cancel()
        }
    }

    private class EmptyApplier : AbstractApplier<Unit>(Unit) {
        override fun insertTopDown(index: Int, instance: Unit) = Unit
        override fun insertBottomUp(index: Int, instance: Unit) = Unit
        override fun remove(index: Int, count: Int) = Unit
        override fun move(from: Int, to: Int, count: Int) = Unit
        override fun onClear() = Unit
    }
}
