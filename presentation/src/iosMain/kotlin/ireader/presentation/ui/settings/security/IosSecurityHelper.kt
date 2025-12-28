package ireader.presentation.ui.settings.security

import kotlinx.cinterop.ExperimentalForeignApi
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics

/**
 * iOS-specific implementation of SecurityHelper.
 * Uses LocalAuthentication framework for biometric availability
 * and a simple hash for credential hashing.
 */
@OptIn(ExperimentalForeignApi::class)
class IosSecurityHelper : SecurityHelper {
    
    override fun isBiometricAvailable(): Boolean {
        val context = LAContext()
        return context.canEvaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            error = null
        )
    }
    
    override fun hashCredential(credential: String): String {
        // Simple hash implementation for cross-platform compatibility
        // For production, consider using platform.Security APIs
        var hash = 7L
        for (char in credential) {
            hash = hash * 31 + char.code
        }
        // Add some additional mixing for better distribution
        hash = hash xor (hash shr 16)
        hash *= 0x85ebca6bL
        hash = hash xor (hash shr 13)
        hash *= 0xc2b2ae35L
        hash = hash xor (hash shr 16)
        return hash.toString(16)
    }
    
    override fun verifyCredential(credential: String, hash: String): Boolean {
        return hashCredential(credential) == hash
    }
}
