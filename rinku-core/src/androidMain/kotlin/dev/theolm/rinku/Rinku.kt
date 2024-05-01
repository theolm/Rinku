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
    intent.dataString?.let {
        treatBeforeFire(
            deepLink = it,
            deepLinkFilter = deepLinkFilter,
            deepLinkMapper = deepLinkMapper,
            fireDeeplink = { Rinku.handleDeepLink(it) }
        )
    }

    val listener = Consumer<Intent> {
        it?.dataString?.let {
            treatBeforeFire(
                deepLink = it,
                deepLinkFilter = deepLinkFilter,
                deepLinkMapper = deepLinkMapper,
                fireDeeplink = { Rinku.handleDeepLink(it) }
            )
        }
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

fun treatBeforeFire(
    deepLink: String,
    deepLinkFilter: DeepLinkFilter? = null,
    deepLinkMapper: DeepLinkMapper? = null,
    fireDeeplink: (String) -> Unit
) {
    val shouldFire = deepLinkFilter?.isValid(deepLink) ?: true
    if (shouldFire) {
        val mappedDeepLink = deepLinkMapper?.map(deepLink) ?: deepLink
        fireDeeplink(mappedDeepLink)
    }
}