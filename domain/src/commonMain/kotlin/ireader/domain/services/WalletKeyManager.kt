package ireader.domain.services

/**
 * Interface for managing Ethereum wallet keys
 * Platform-specific implementations handle secure key storage
 */
interface WalletKeyManager {
    /**
     * Get or generate an Ethereum key pair for the user
     * @return Pair of (address, privateKey) where address is the Ethereum address (0x...)
     * and privateKey is the hex-encoded private key
     */
    suspend fun getOrCreateKeyPair(): Pair<String, String>
    
    /**
     * Get the current wallet address if it exists
     * @return The Ethereum address or null if no key pair exists
     */
    suspend fun getAddress(): String?
    
    /**
     * Clear the stored key pair (for logout/reset)
     */
    suspend fun clearKeys()
}
