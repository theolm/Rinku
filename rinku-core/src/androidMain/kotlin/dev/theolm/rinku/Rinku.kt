package dev.theolm.rinku

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.core.util.Consumer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dev.theolm.rinku.models.DeepLinkFilter

fun ComponentActivity.RinkuInit(deepLinkFilter: DeepLinkFilter? = null) {
    intent.dataString?.let {
        val shouldFire = deepLinkFilter?.isValid(it) ?: true
        if (shouldFire) {
            Rinku.handleDeepLink(it)
        }
    }

    val listener = Consumer<Intent> {
        it?.dataString?.let {
            val shouldFire = deepLinkFilter?.isValid(it) ?: true
            if (shouldFire) {
                Rinku.handleDeepLink(it)
            }
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
