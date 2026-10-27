package com.presa.app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.presa.app.data.LocaleStore
import com.presa.app.ui.PresaApp

class MainActivity : ComponentActivity() {
    private val container
        get() = (application as PresaApplication).container

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleStore.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        setContent {
            PresaApp(
                container = container,
                onRecreateForLocale = { recreate() },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        container.hidManager.onPermissionsChanged()
    }

    // ComponentActivity.dispatchKeyEvent is restricted to the AndroidX library
    // group, but volume keys must be consumed before the window handles them.
    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val isVolumeKey = event.keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        if (isVolumeKey) {
            val handled = container.volumeKeys.dispatch(
                isNext = event.keyCode == KeyEvent.KEYCODE_VOLUME_UP,
                isDown = event.action == KeyEvent.ACTION_DOWN,
                repeatCount = event.repeatCount,
            )
            if (handled) return true
        }
        return super.dispatchKeyEvent(event)
    }
}
