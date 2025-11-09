package ireader.domain.data.repository

import ireader.domain.models.security.AuthMethod

/**
 * Repository interface for security-related operations
 */
interface SecurityRepository {
    /**
     * Set the authentication method for app lock
     * @param method The authentication method to set
     * @return Result indicating success or failure
     */
    suspend fun setAuthMethod(method: AuthMethod): Result<Unit>
    
    /**
     * Get the current authentication method
     * @return The current authentication method
     */
    suspend fun getAuthMethod(): AuthMethod
    
    /**
     * Authenticate using PIN or Password
     * @param input The PIN or password to verify
     * @return Result indicating if authentication was successful
     */
    suspend fun authenticate(input: String): Result<Boolean>
    
    /**
     * Authenticate using biometric
     * @return Result indicating if authentication was successful
     */
    suspend fun authenticateBiometric(): Result<Boolean>
    
    /**
     * Check if authentication is enabled
     * @return True if any authentication method is enabled
     */
    suspend fun isAuthEnabled(): Boolean
    
    /**
     * Clear the authentication method (disable app lock)
     * @return Result indicating success or failure
     */
    suspend fun clearAuthMethod(): Result<Unit>
    
    /**
     * Verify if biometric authentication is available on the device
     * @return True if biometric authentication is available
     */
    suspend fun isBiometricAvailable(): Boolean
}
