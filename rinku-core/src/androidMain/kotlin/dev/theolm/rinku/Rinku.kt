package dev.theolm.rinku

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.core.util.Consumer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

fun ComponentActivity.RinkuInit() {
    intent.dataString?.let {
        Rinku.handleDeepLink(it)
    }

    val listener = Consumer<Intent> {
        it?.dataString?.let {
            Rinku.handleDeepLink(it)
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
