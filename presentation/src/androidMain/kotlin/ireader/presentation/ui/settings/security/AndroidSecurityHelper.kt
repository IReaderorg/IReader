package ireader.presentation.ui.settings.security

import android.content.Context
import androidx.biometric.BiometricManager
import java.security.MessageDigest

/**
 * Android-specific implementation of SecurityHelper
 * Uses BiometricManager for biometric availability and SHA-256 for credential hashing
 */
class AndroidSecurityHelper(private val context: Context) : SecurityHelper {
    
    override fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or 
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
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
