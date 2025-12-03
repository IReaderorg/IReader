package ireader.domain.services.platform

import ireader.domain.services.common.PlatformService
import kotlinx.coroutines.flow.Flow
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Platform-agnostic device information service
 * 
 * Provides information about the device hardware, screen, and capabilities
 */
interface DeviceInfoService : PlatformService {
    
    /**
     * Check if device is a tablet
     * 
     * @return true if device is a tablet, false if phone/desktop
     */
    fun isTablet(): Boolean
    
    /**
     * Check if device is in landscape orientation
     * 
     * @return true if landscape, false if portrait
     */
    fun isLandscape(): Boolean
    
    /**
     * Get device type
     * 
     * @return Device type (phone, tablet, desktop)
     */
    fun getDeviceType(): DeviceType
    
    /**
     * Get screen size in DP
     * 
     * @return Screen size information
     */
    fun getScreenSize(): ScreenSize
    
    /**
     * Get screen density
     * 
     * @return Screen density (DPI)
     */
    fun getScreenDensity(): Float
    
    /**
     * Observe orientation changes
     * 
     * @return Flow of orientation events
     */
    fun observeOrientationChanges(): Flow<OrientationEvent>
    
    /**
     * Get device model name
     * 
     * @return Device model (e.g., "Pixel 6", "MacBook Pro")
     */
    fun getDeviceModel(): String
    
    /**
     * Get OS version
     * 
     * @return OS version string
     */
    fun getOSVersion(): String
    
    /**
     * Check if device has specific capability
     * 
     * @param capability Capability to check
     * @return true if device has the capability
     */
    fun hasCapability(capability: DeviceCapability): Boolean
}

/**
 * Device type enumeration
 */
enum class DeviceType {
    PHONE,
    TABLET,
    DESKTOP,
    TV,
    WATCH,
    UNKNOWN
}

/**
 * Screen size information
 */
data class ScreenSize(
    val widthDp: Int,
    val heightDp: Int,
    val widthPx: Int,
    val heightPx: Int,
    val smallestWidthDp: Int
) {
    val isCompact: Boolean
        get() = smallestWidthDp < 600
    
    val isMedium: Boolean
        get() = smallestWidthDp in 600..839
    
    val isExpanded: Boolean
        get() = smallestWidthDp >= 840
}

/**
 * Orientation event
 */
data class OrientationEvent(
    val isLandscape: Boolean,
    val rotation: Int, // 0, 90, 180, 270
    val timestamp: Long = currentTimeToLong()
)

/**
 * Device capabilities
 */
enum class DeviceCapability {
    BIOMETRIC_AUTH,
    NFC,
    GPS,
    CAMERA,
    MICROPHONE,
    BLUETOOTH,
    CELLULAR,
    WIFI,
    HAPTIC_FEEDBACK,
    ACCELEROMETER,
    GYROSCOPE,
    MAGNETOMETER,
    PROXIMITY_SENSOR,
    LIGHT_SENSOR,
    FINGERPRINT_SENSOR,
    FACE_RECOGNITION,
    STYLUS_SUPPORT
}
