package dev.theolm.rinku

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.core.util.Consumer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dev.theolm.rinku.models.DeepLinkFilter
import dev.theolm.rinku.models.DeepLinkMapper

fun ComponentActivity.RinkuInit(
    deepLinkFilter: DeepLinkFilter? = null,
    deepLinkMapper: DeepLinkMapper? = null
) {
    deepLinkFilter?.let { Rinku.setDeepLinkFilter(it) }
    deepLinkMapper?.let { Rinku.setDeepLinkMapper(it) }

    intent.treatAndFireDeepLink(deepLinkFilter, deepLinkMapper)

    val listener = Consumer<Intent> {
        it?.treatAndFireDeepLink(deepLinkFilter, deepLinkMapper)
    }

    lifecycle.addObserver(
        object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                this@RinkuInit.addOnNewIntentListener(listener)
                super.onCreate(owner)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                this@RinkuInit.removeOnNewIntentListener(listener)
                super.onDestroy(owner)
            }
        }
    )
}

fun Intent.treatAndFireDeepLink(
    deepLinkFilter: DeepLinkFilter? = null,
    deepLinkMapper: DeepLinkMapper? = null,
) {
    dataString?.let { deepLink ->
        val shouldFire = deepLinkFilter?.isValid(deepLink) ?: true
        if (shouldFire) {
            val mappedDeepLink = deepLinkMapper?.map(deepLink) ?: deepLink
            Rinku.handleDeepLink(mappedDeepLink)
        }
    }
}
