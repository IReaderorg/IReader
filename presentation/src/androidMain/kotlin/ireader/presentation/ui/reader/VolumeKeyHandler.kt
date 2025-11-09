package ireader.presentation.ui.reader

import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import android.view.View
import android.view.ViewTreeObserver

/**
 * Android implementation of volume key handler for reader navigation
 */
@Composable
actual fun Modifier.volumeKeyHandler(
    enabled: Boolean,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit
): Modifier {
    val view = LocalView.current
    
    DisposableEffect(enabled, onVolumeUp, onVolumeDown) {
        if (!enabled) {
            return@DisposableEffect onDispose { }
        }
        
        val keyListener = View.OnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_VOLUME_UP -> {
                        onVolumeUp()
                        true
                    }
                    KeyEvent.KEYCODE_VOLUME_DOWN -> {
                        onVolumeDown()
                        true
                    }
                    else -> false
                }
            } else {
                false
            }
        }
        
        view.setOnKeyListener(keyListener)
        view.isFocusableInTouchMode = true
        view.requestFocus()
        
        onDispose {
            view.setOnKeyListener(null)
        }
    }
    
    return this
}
