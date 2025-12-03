package ireader.domain.services.platform

import ireader.domain.services.common.PlatformService
import ireader.domain.services.common.ServiceResult
import kotlinx.coroutines.flow.Flow
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Platform-agnostic system interaction service
 * 
 * Handles system-level interactions like brightness control, volume control,
 * screen security, and hardware key events.
 */
interface SystemInteractionService : PlatformService {
    
    /**
     * Get current screen brightness
     * 
     * @return Brightness value between 0.0 (darkest) and 1.0 (brightest)
     */
    suspend fun getBrightness(): Float
    
    /**
     * Set screen brightness
     * 
     * @param brightness Value between 0.0 (darkest) and 1.0 (brightest)
     * @return Result indicating success or error
     */
    suspend fun setBrightness(brightness: Float): ServiceResult<Unit>
    
    /**
     * Check if brightness control is supported on this platform
     * 
     * @return true if brightness control is available
     */
    fun isBrightnessControlSupported(): Boolean
    
    /**
     * Get current system volume
     * 
     * @return Volume value between 0.0 (mute) and 1.0 (max)
     */
    suspend fun getVolume(): Float
    
    /**
     * Set system volume
     * 
     * @param volume Value between 0.0 (mute) and 1.0 (max)
     * @return Result indicating success or error
     */
    suspend fun setVolume(volume: Float): ServiceResult<Unit>
    
    /**
     * Observe volume key events (volume up/down hardware buttons)
     * 
     * @return Flow of volume key events
     */
    fun observeVolumeKeys(): Flow<VolumeKeyEvent>
    
    /**
     * Enable or disable secure screen (prevents screenshots/screen recording)
     * 
     * @param enabled true to enable secure mode, false to disable
     * @return Result indicating success or error
     */
    suspend fun setSecureScreen(enabled: Boolean): ServiceResult<Unit>
    
    /**
     * Check if secure screen is supported on this platform
     * 
     * @return true if secure screen is available
     */
    fun isSecureScreenSupported(): Boolean
    
    /**
     * Keep screen on (prevent auto-sleep)
     * 
     * @param enabled true to keep screen on, false to allow auto-sleep
     * @return Result indicating success or error
     */
    suspend fun setKeepScreenOn(enabled: Boolean): ServiceResult<Unit>
    
    /**
     * Check if device is in landscape orientation
     * 
     * @return true if landscape, false if portrait
     */
    fun isLandscape(): Boolean
    
    /**
     * Check if device is a tablet
     * 
     * @return true if tablet, false if phone/desktop
     */
    fun isTablet(): Boolean
    
    /**
     * Observe orientation changes
     * 
     * @return Flow of orientation change events
     */
    fun observeOrientationChanges(): Flow<OrientationEvent>
}

/**
 * Volume key event
 */
data class VolumeKeyEvent(
    val type: VolumeKeyType,
    val timestamp: Long = currentTimeToLong()
)

enum class VolumeKeyType {
    VOLUME_UP,
    VOLUME_DOWN
}
