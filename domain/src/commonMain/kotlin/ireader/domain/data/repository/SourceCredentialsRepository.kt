package ireader.domain.data.repository

/**
 * Repository for managing source credentials
 */
interface SourceCredentialsRepository {
    /**
     * Store credentials for a source
     * @param sourceId The unique identifier of the source
     * @param username The username
     * @param password The password
     */
    suspend fun storeCredentials(sourceId: Long, username: String, password: String)
    
    /**
     * Get credentials for a source
     * @param sourceId The unique identifier of the source
     * @return Pair of username and password, or null if not found
     */
    suspend fun getCredentials(sourceId: Long): Pair<String, String>?
    
    /**
     * Remove credentials for a source
     * @param sourceId The unique identifier of the source
     */
    suspend fun removeCredentials(sourceId: Long)
    
    /**
     * Check if credentials exist for a source
     * @param sourceId The unique identifier of the source
     * @return true if credentials exist, false otherwise
     */
    suspend fun hasCredentials(sourceId: Long): Boolean
}
