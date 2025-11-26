package ireader.domain.services.platform

/**
 * Desktop implementation of PlatformCapabilities
 */
class DesktopPlatformCapabilities : PlatformCapabilities {
    
    override val platformType: PlatformType = PlatformType.DESKTOP
    
    override val platformVersion: String
        get() = "${System.getProperty("os.name")} ${System.getProperty("os.version")}"
    
    override val hasFileSystemAccess: Boolean = true
    
    override val hasNotificationSupport: Boolean = true
    
    override val hasBackgroundServiceSupport: Boolean = true
    
    override val hasTTSSupport: Boolean = true
    
    override val hasBiometricSupport: Boolean = false
    
    override val hasCameraSupport: Boolean = false
    
    override val hasVibrationSupport: Boolean = false
    
    override val deviceModel: String
        get() = "Desktop"
    
    override val deviceManufacturer: String
        get() = System.getProperty("os.name") ?: "Unknown"
    
    override val isEmulator: Boolean = false
}
