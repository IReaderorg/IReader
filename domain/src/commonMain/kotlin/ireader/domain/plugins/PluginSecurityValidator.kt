package ireader.domain.plugins

import kotlinx.serialization.Serializable

/**
 * Security validator for plugin installations.
 * Ensures paid plugins can only be installed on apps using the official Supabase backend.
 * 
 * Security Model:
 * 1. Free plugins - Can be installed anywhere
 * 2. Paid plugins - Require validation against official backend
 * 3. License validation - Checks user purchase/grant status on official backend
 * 4. App signature verification - Ensures app is using official Supabase URL
 */
interface PluginSecurityValidator {
    
    /**
     * Validate if a plugin can be installed on this app instance
     */
    suspend fun canInstallPlugin(pluginId: String, monetizationType: String): PluginInstallValidation
    
    /**
     * Validate user's license for a paid plugin
     */
    suspend fun validateLicense(userId: String, pluginId: String): LicenseValidation
    
    /**
     * Check if app is using official Supabase backend
     */
    suspend fun isOfficialBackend(): Boolean
    
    /**
     * Get device/app fingerprint for license binding
     */
    fun getAppFingerprint(): String
    
    /**
     * Register device for license
     */
    suspend fun registerDevice(userId: String, pluginId: String): Result<DeviceRegistration>
}

@Serializable
data class PluginInstallValidation(
    val canInstall: Boolean,
    val reason: InstallBlockReason? = null,
    val requiresLicense: Boolean = false,
    val licenseStatus: LicenseStatus? = null
)

@Serializable
enum class InstallBlockReason {
    UNOFFICIAL_BACKEND,      // App not using official Supabase
    NO_LICENSE,              // User hasn't purchased
    LICENSE_EXPIRED,         // License expired
    DEVICE_LIMIT_REACHED,    // Too many devices registered
    PLUGIN_REVOKED,          // Plugin removed from store
    REGION_RESTRICTED,       // Not available in user's region
    VERSION_MISMATCH         // App version too old
}

@Serializable
data class LicenseValidation(
    val isValid: Boolean,
    val status: LicenseStatus,
    val expiryDate: Long? = null,
    val deviceCount: Int = 0,
    val maxDevices: Int = 3,
    val grantedBy: String? = null  // If granted by developer
)

@Serializable
enum class LicenseStatus {
    VALID,
    EXPIRED,
    NOT_FOUND,
    REVOKED,
    TRIAL,
    GRANTED  // Free access granted by developer
}

@Serializable
data class DeviceRegistration(
    val deviceId: String,
    val registeredAt: Long,
    val isActive: Boolean
)
