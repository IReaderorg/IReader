package ireader.presentation.ui.reader.components

/**
 * Platform-specific brightness manager for controlling screen brightness
 * Requirements: 15.1, 15.2, 15.3, 15.4, 15.5
 * 
 * @deprecated Use SystemInteractionService from domain layer instead
 * 
 * This class is deprecated and will be removed in a future release.
 * Use SystemInteractionService for brightness control.
 * 
 * Migration example:
 * ```
 * // Before
 * val brightnessManager = rememberBrightnessManager()
 * brightnessManager.setBrightness(0.5f)
 * 
 * // After
 * val systemService: SystemInteractionService // Inject in ViewModel
 * scope.launch {
 *     systemService.setBrightness(0.5f)
 * }
 * ```
 */
@Deprecated(
    message = "Use SystemInteractionService from domain layer instead",
    replaceWith = ReplaceWith(
        "systemInteractionService.setBrightness(value)",
        "ireader.domain.services.platform.SystemInteractionService"
    ),
    level = DeprecationLevel.WARNING
)
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
