package ireader.presentation.ui.reader.components

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Android implementation of BrightnessManager
 * Uses WindowManager.LayoutParams.screenBrightness for brightness control
 */
actual class BrightnessManager(private val activity: Activity?) {
    
    actual fun getBrightness(): Float {
        return activity?.window?.attributes?.screenBrightness ?: 0.5f
    }
    
    actual fun setBrightness(value: Float) {
        activity?.window?.let { window ->
            val layoutParams = window.attributes
            layoutParams.screenBrightness = value.coerceIn(0.1f, 1.0f)
            window.attributes = layoutParams
        }
    }
    
    actual fun isSupported(): Boolean {
        return activity != null
    }
}

/**
 * Composable function to create a BrightnessManager instance
 */
@Composable
fun rememberBrightnessManager(): BrightnessManager {
    val context = LocalContext.current
    return remember(context) {
        BrightnessManager(context as? Activity)
    }
}
