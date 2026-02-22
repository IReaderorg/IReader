package ireader.domain.services.sync

/**
 * Service for securely storing encryption keys.
 * 
 * Task 9.2.5: Secure key storage
 * 
 * Platform-specific implementations:
 * - Android: Uses Android Keystore System
 * - Desktop: Uses Java Keystore (JKS)
 * 
 * Keys are stored encrypted and protected by the platform's secure storage.
 */
interface KeyStorageService {
    
    /**
     * Store an encryption key securely.
     * 
     * @param alias Unique identifier for the key
     * @param key The encryption key to store (typically 32 bytes for AES-256)
     * @return Result indicating success or failure
     */
    suspend fun storeKey(alias: String, key: ByteArray): Result<Unit>
    
    /**
     * Retrieve a stored encryption key.
     * 
     * @param alias Unique identifier for the key
     * @return Result containing the key or failure
     */
    suspend fun retrieveKey(alias: String): Result<ByteArray>
    
    /**
     * Delete a stored encryption key.
     * 
     * @param alias Unique identifier for the key
     * @return Result indicating success or failure
     */
    suspend fun deleteKey(alias: String): Result<Unit>
    
    /**
     * Check if a key exists in secure storage.
     * 
     * @param alias Unique identifier for the key
     * @return true if key exists, false otherwise
     */
    suspend fun keyExists(alias: String): Boolean
    
    /**
     * List all stored key aliases.
     * 
     * @return List of key aliases
     */
    suspend fun listKeys(): List<String>
}
