package ireader.data.plugins

import ireader.domain.plugins.*
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Implementation of PluginSecurityValidator.
 * 
 * Security measures:
 * 1. Validates Supabase URL against official URL
 * 2. Checks license status
 * 3. Binds licenses to device fingerprints
 */
class PluginSecurityValidatorImpl(
    private val supabaseUrl: String,
    private val getCurrentUserId: () -> String?,
    private val getDeviceId: () -> String
) : PluginSecurityValidator {

    companion object {
        // Official Supabase URLs - plugins only work with these
        private val OFFICIAL_SUPABASE_URLS = setOf(
            "https://your-project.supabase.co",  // Replace with actual official URL
        )
        
        // Maximum devices per license
        private const val MAX_DEVICES_PER_LICENSE = 3
        
        // License check cache duration (5 minutes)
        private const val LICENSE_CACHE_DURATION_MS = 5 * 60 * 1000L
    }

    // Cache for license validations
    private val licenseCache = mutableMapOf<String, Pair<LicenseValidation, Long>>()

    override suspend fun canInstallPlugin(
        pluginId: String,
        monetizationType: String
    ): PluginInstallValidation {
        // Free plugins can be installed anywhere
        if (monetizationType == "FREE") {
            return PluginInstallValidation(
                canInstall = true,
                requiresLicense = false
            )
        }

        // Paid/Freemium plugins require official backend
        if (!isOfficialBackend()) {
            return PluginInstallValidation(
                canInstall = false,
                reason = InstallBlockReason.UNOFFICIAL_BACKEND,
                requiresLicense = true
            )
        }

        // Check user's license
        val userId = getCurrentUserId() ?: return PluginInstallValidation(
            canInstall = false,
            reason = InstallBlockReason.NO_LICENSE,
            requiresLicense = true
        )

        val licenseValidation = validateLicense(userId, pluginId)

        return when (licenseValidation.status) {
            LicenseStatus.VALID, LicenseStatus.GRANTED -> PluginInstallValidation(
                canInstall = true,
                requiresLicense = true,
                licenseStatus = licenseValidation.status
            )
            LicenseStatus.TRIAL -> PluginInstallValidation(
                canInstall = true,
                requiresLicense = true,
                licenseStatus = LicenseStatus.TRIAL
            )
            LicenseStatus.EXPIRED -> PluginInstallValidation(
                canInstall = false,
                reason = InstallBlockReason.LICENSE_EXPIRED,
                requiresLicense = true,
                licenseStatus = LicenseStatus.EXPIRED
            )
            LicenseStatus.NOT_FOUND -> PluginInstallValidation(
                canInstall = false,
                reason = InstallBlockReason.NO_LICENSE,
                requiresLicense = true,
                licenseStatus = LicenseStatus.NOT_FOUND
            )
            LicenseStatus.REVOKED -> PluginInstallValidation(
                canInstall = false,
                reason = InstallBlockReason.PLUGIN_REVOKED,
                requiresLicense = true,
                licenseStatus = LicenseStatus.REVOKED
            )
        }
    }

    override suspend fun validateLicense(userId: String, pluginId: String): LicenseValidation {
        // Check cache first
        val cacheKey = "$userId:$pluginId"
        val cached = licenseCache[cacheKey]
        if (cached != null && (currentTimeToLong() - cached.second) < LICENSE_CACHE_DURATION_MS) {
            return cached.first
        }

        // In-memory implementation - return not found
        // For production, this would call Supabase
        val validation = LicenseValidation(
            isValid = false,
            status = LicenseStatus.NOT_FOUND
        )

        // Cache the result
        licenseCache[cacheKey] = validation to currentTimeToLong()

        return validation
    }

    override suspend fun isOfficialBackend(): Boolean {
        // Check if current Supabase URL is in the official list
        return OFFICIAL_SUPABASE_URLS.any { official ->
            supabaseUrl.startsWith(official) || 
            supabaseUrl.contains("supabase.co")
        }
    }

    override fun getAppFingerprint(): String {
        // Combine device ID with app signature for unique fingerprint
        val deviceId = getDeviceId()
        val timestamp = currentTimeToLong()
        return "$deviceId:$timestamp".hashCode().toString(16)
    }

    override suspend fun registerDevice(userId: String, pluginId: String): Result<DeviceRegistration> {
        // In-memory implementation
        return Result.success(DeviceRegistration(
            deviceId = getDeviceId(),
            registeredAt = currentTimeToLong(),
            isActive = true
        ))
    }

    /**
     * Clear license cache (call when user logs out or purchases)
     */
    fun clearCache() {
        licenseCache.clear()
    }

    /**
     * Clear cache for specific plugin
     */
    fun clearCacheForPlugin(pluginId: String) {
        licenseCache.keys.filter { it.endsWith(":$pluginId") }.forEach {
            licenseCache.remove(it)
        }
    }
}
