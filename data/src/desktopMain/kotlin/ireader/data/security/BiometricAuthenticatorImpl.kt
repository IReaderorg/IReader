package ireader.data.security

/**
 * Desktop stub implementation of BiometricAuthenticator
 * Biometric authentication is not supported on desktop
 */
class BiometricAuthenticatorImpl : BiometricAuthenticator {
    
    override fun isBiometricAvailable(): Boolean {
        return false
    }
    
    override suspend fun authenticate(): Result<Boolean> {
        return Result.failure(Exception("Biometric authentication not supported on desktop"))
    }
}
