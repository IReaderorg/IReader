package ireader.domain.models.security

/**
 * Represents different authentication methods for app lock
 */
sealed class AuthMethod {
    /**
     * PIN-based authentication (4-6 digits)
     */
    data class PIN(val pin: String) : AuthMethod()
    
    /**
     * Password-based authentication
     */
    data class Password(val password: String) : AuthMethod()
    
    /**
     * Biometric authentication (fingerprint, face recognition)
     */
    object Biometric : AuthMethod()
    
    /**
     * No authentication required
     */
    object None : AuthMethod()
}
