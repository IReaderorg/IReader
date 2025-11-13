package ireader.presentation.ui.reader.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIScreen

/**
 * iOS implementation of BrightnessManager
 * Uses UIScreen.mainScreen.brightness for brightness control
 */
actual class BrightnessManager {
    
    actual fun getBrightness(): Float {
        return UIScreen.mainScreen.brightness.toFloat()
    }
    
    actual fun setBrightness(value: Float) {
        UIScreen.mainScreen.brightness = value.coerceIn(0.1, 1.0).toDouble()
    }
    
    actual fun isSupported(): Boolean {
        return true
    }
}

/**
 * Composable function to create a BrightnessManager instance
 */
@Composable
fun rememberBrightnessManager(): BrightnessManager {
    return remember {
        BrightnessManager()
    }
}
