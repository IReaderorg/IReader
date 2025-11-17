package ireader.domain.catalogs.interactor

import ireader.domain.models.entities.ExtensionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Manages extension repositories following Mihon's repository system
 */
interface ExtensionRepositoryManager {
    
    /**
     * Get all configured repositories
     */
    fun getRepositories(): Flow<List<ExtensionRepository>>
    
    /**
     * Add a new repository
     */
    suspend fun addRepository(
        name: String,
        url: String,
        fingerprint: String? = null
    ): Result<ExtensionRepository>
    
    /**
     * Remove a repository
     */
    suspend fun removeRepository(repositoryId: Long): Result<Unit>
    
    /**
     * Update repository
     */
    suspend fun updateRepository(repository: ExtensionRepository): Result<Unit>
    
    /**
     * Sync repository (fetch latest extensions)
     */
    suspend fun syncRepository(repositoryId: Long): Result<Unit>
    
    /**
     * Sync all enabled repositories
     */
    suspend fun syncAllRepositories(): Result<Unit>
    
    /**
     * Verify repository fingerprint
     */
    suspend fun verifyRepositoryFingerprint(repositoryId: Long): Boolean
    
    /**
     * Enable/disable repository
     */
    suspend fun setRepositoryEnabled(repositoryId: Long, enabled: Boolean): Result<Unit>
    
    /**
     * Set repository auto-update
     */
    suspend fun setRepositoryAutoUpdate(repositoryId: Long, autoUpdate: Boolean): Result<Unit>
}
