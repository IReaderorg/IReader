package ireader.presentation.ui.reader.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Desktop implementation of BrightnessManager
 * Note: Desktop brightness control is limited and platform-dependent
 * This implementation stores the preference but doesn't control system brightness
 */
actual class BrightnessManager {
    private var currentBrightness: Float = 0.5f
    
    actual fun getBrightness(): Float {
        return currentBrightness
    }
    
    actual fun setBrightness(value: Float) {
        currentBrightness = value.coerceIn(0.1f, 1.0f)
        // Desktop brightness control would require platform-specific APIs
        // For now, we just store the value for UI overlay purposes
    }
    
    actual fun isSupported(): Boolean {
        // Desktop brightness control is limited
        return false
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
