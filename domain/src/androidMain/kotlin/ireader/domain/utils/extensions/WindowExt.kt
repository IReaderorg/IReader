package ireader.domain.utils.extensions

import android.view.Window
import android.view.WindowManager

/**
 * Sets the secure screen flag on the window
 * When enabled, prevents screenshots and screen recording
 */
fun Window.setSecureScreen(enabled: Boolean) {
    if (enabled) {
        setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    } else {
        clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}
