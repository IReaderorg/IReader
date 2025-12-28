package ireader.presentation.ui.settings.security

import java.security.MessageDigest

/**
 * Desktop-specific implementation of SecurityHelper.
 * Desktop platforms generally don't have biometric hardware accessible via standard APIs.
 */
class DesktopSecurityHelper : SecurityHelper {
    
    override fun isBiometricAvailable(): Boolean {
        // Desktop platforms don't have standard biometric APIs
        // Windows Hello, macOS Touch ID would require platform-specific native code
        return false
    }
    
    override fun hashCredential(credential: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(credential.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    override fun verifyCredential(credential: String, hash: String): Boolean {
        return hashCredential(credential) == hash
    }
}
