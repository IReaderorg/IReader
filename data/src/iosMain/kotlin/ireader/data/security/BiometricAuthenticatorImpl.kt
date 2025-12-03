package ireader.data.security

/**
 * iOS implementation of BiometricAuthenticator
 * Uses LocalAuthentication framework for Face ID / Touch ID
 * 
 * TODO: Implement actual biometric authentication using:
 * - platform.LocalAuthentication.LAContext
 * - LAPolicy.deviceOwnerAuthenticationWithBiometrics
 */
class BiometricAuthenticatorImpl : BiometricAuthenticator {
    
    override fun isBiometricAvailable(): Boolean {
        // TODO: Check LAContext.canEvaluatePolicy
        return false
    }
    
    override suspend fun authenticate(): Result<Boolean> {
        // TODO: Implement using LAContext.evaluatePolicy
        return Result.failure(Exception("Biometric authentication not yet implemented for iOS"))
    }
}
