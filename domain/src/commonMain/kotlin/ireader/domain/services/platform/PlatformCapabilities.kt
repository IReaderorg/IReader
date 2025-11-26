package ireader.domain.services.platform

/**
 * Platform capabilities and information
 * 
 * Provides information about the current platform and its capabilities.
 */
interface PlatformCapabilities {
    
    /**
     * Get the current platform type
     */
    val platformType: PlatformType
    
    /**
     * Get platform version (e.g., Android API level, iOS version)
     */
    val platformVersion: String
    
    /**
     * Check if file system access is available
     */
    val hasFileSystemAccess: Boolean
    
    /**
     * Check if notifications are supported
     */
    val hasNotificationSupport: Boolean
    
    /**
     * Check if background services are supported
     */
    val hasBackgroundServiceSupport: Boolean
    
    /**
     * Check if TTS (Text-to-Speech) is supported
     */
    val hasTTSSupport: Boolean
    
    /**
     * Check if biometric authentication is available
     */
    val hasBiometricSupport: Boolean
    
    /**
     * Check if the device has a camera
     */
    val hasCameraSupport: Boolean
    
    /**
     * Check if the device supports vibration
     */
    val hasVibrationSupport: Boolean
    
    /**
     * Get device model/name
     */
    val deviceModel: String
    
    /**
     * Get device manufacturer
     */
    val deviceManufacturer: String
    
    /**
     * Check if running on emulator/simulator
     */
    val isEmulator: Boolean
}

/**
 * Platform type enumeration
 */
enum class PlatformType {
    ANDROID,
    DESKTOP,
    IOS,
    WEB
}
