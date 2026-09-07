package ireader.presentation.ui.reader

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.KeyEvent
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

/**
 * Global dispatcher for volume key navigation events on Android.
 * Allows both Window.Callback wrapping and MainActivity.dispatchKeyEvent interception.
 */
object AndroidVolumeKeyDispatcher {
    private var activeHandler: ((keyCode: Int, event: KeyEvent) -> Boolean)? = null

    fun setHandler(handler: ((keyCode: Int, event: KeyEvent) -> Boolean)?) {
        activeHandler = handler
    }

    fun handleKeyEvent(event: KeyEvent): Boolean {
        return activeHandler?.invoke(event.keyCode, event) ?: false
    }
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

/**
 * Android implementation of volume key handler for reader navigation.
 * Intercepts hardware volume keys at the Window.Callback level and via
 * AndroidVolumeKeyDispatcher in MainActivity, preventing the Android OS
 * system volume HUD dialog from appearing and reliably turning pages/scrolling.
 */
@Composable
actual fun Modifier.volumeKeyHandler(
    enabled: Boolean,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit
): Modifier {
    val context = LocalContext.current
    val currentOnVolumeUp by rememberUpdatedState(onVolumeUp)
    val currentOnVolumeDown by rememberUpdatedState(onVolumeDown)

    DisposableEffect(enabled, context) {
        if (!enabled) {
            return@DisposableEffect onDispose { }
        }

        val activity = context.findActivity()
        val window = activity?.window

        val handleKey: (Int, KeyEvent) -> Boolean = { keyCode, event ->
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                        currentOnVolumeUp()
                    }
                    true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                        currentOnVolumeDown()
                    }
                    true
                }
                else -> false
            }
        }

        // 1. Register with the global dispatcher (for MainActivity.dispatchKeyEvent / onKeyDown)
        AndroidVolumeKeyDispatcher.setHandler(handleKey)

        // 2. Wrap window.callback on Activity's window for window-level root interception
        val originalCallback = window?.callback
        if (window != null && originalCallback != null) {
            val customCallback = object : Window.Callback by originalCallback {
                override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
                    if (event != null && handleKey(event.keyCode, event)) {
                        return true
                    }
                    return originalCallback.dispatchKeyEvent(event)
                }
            }
            window.callback = customCallback

            onDispose {
                AndroidVolumeKeyDispatcher.setHandler(null)
                if (window.callback === customCallback) {
                    window.callback = originalCallback
                }
            }
        } else {
            onDispose {
                AndroidVolumeKeyDispatcher.setHandler(null)
            }
        }
    }

    return this
}
