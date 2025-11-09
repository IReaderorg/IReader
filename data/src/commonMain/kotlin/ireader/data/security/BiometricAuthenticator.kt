package ireader.data.security

/**
 * Platform-specific biometric authentication interface
 */
interface BiometricAuthenticator {
    /**
     * Check if biometric authentication is available on the device
     */
    fun isBiometricAvailable(): Boolean
    
    /**
     * Authenticate using biometric
     * @return Result indicating if authentication was successful
     */
    suspend fun authenticate(): Result<Boolean>
}
