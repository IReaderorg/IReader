package ireader.presentation.ui.reader.components

/**
 * Platform-specific brightness manager for controlling screen brightness
 * Requirements: 15.1, 15.2, 15.3, 15.4, 15.5
 */
expect class BrightnessManager {
    /**
     * Get current screen brightness
     * @return brightness value between 0.0 and 1.0
     */
    fun getBrightness(): Float
    
    /**
     * Set screen brightness
     * @param value brightness value between 0.0 and 1.0
     */
    fun setBrightness(value: Float)
    
    /**
     * Check if brightness control is supported on this platform
     */
    fun isSupported(): Boolean
}
