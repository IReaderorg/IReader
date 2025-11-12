package ireader.domain.services

/**
 * Interface for secure session storage
 * Platform-specific implementations handle secure storage of authentication tokens and user data
 */
interface SecureSessionStorage {
    
    /**
     * Store the authenticated wallet address securely
     * 
     * @param walletAddress The wallet address to store
     */
    suspend fun storeWalletAddress(walletAddress: String)
    
    /**
     * Retrieve the stored wallet address
     * 
     * @return The stored wallet address, or null if not found
     */
    suspend fun getWalletAddress(): String?
    
    /**
     * Store a session token securely
     * 
     * @param token The session token to store
     */
    suspend fun storeSessionToken(token: String)
    
    /**
     * Retrieve the stored session token
     * 
     * @return The stored session token, or null if not found
     */
    suspend fun getSessionToken(): String?
    
    /**
     * Clear all stored session data
     */
    suspend fun clearSession()
    
    /**
     * Check if a valid session exists
     * 
     * @return true if a session exists, false otherwise
     */
    suspend fun hasValidSession(): Boolean
}
