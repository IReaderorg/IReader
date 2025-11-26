package ireader.domain.services.platform

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Android implementation of PlatformCapabilities
 */
class AndroidPlatformCapabilities(
    private val context: Context
) : PlatformCapabilities {
    
    override val platformType: PlatformType = PlatformType.ANDROID
    
    override val platformVersion: String
        get() = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    
    override val hasFileSystemAccess: Boolean = true
    
    override val hasNotificationSupport: Boolean = true
    
    override val hasBackgroundServiceSupport: Boolean = true
    
    override val hasTTSSupport: Boolean = true
    
    override val hasBiometricSupport: Boolean
        get() = context.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && 
                 context.packageManager.hasSystemFeature(PackageManager.FEATURE_FACE))
    
    override val hasCameraSupport: Boolean
        get() = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    
    override val hasVibrationSupport: Boolean
        get() = try {
            @Suppress("DEPRECATION")
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.getSystemService(android.os.Vibrator::class.java)
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            }
            vibrator?.hasVibrator() ?: false
        } catch (e: Exception) {
            false
        }
    
    override val deviceModel: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}"
    
    override val deviceManufacturer: String
        get() = Build.MANUFACTURER
    
    override val isEmulator: Boolean
        get() = Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
                "google_sdk" == Build.PRODUCT
}
