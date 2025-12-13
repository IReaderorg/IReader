package ireader.domain.plugins

import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing plugin repositories (sources).
 * Users can add custom repositories to discover and install plugins.
 */
interface PluginRepositoryRepository {
    
    /**
     * Get all plugin repositories
     */
    fun getAll(): Flow<List<PluginRepositoryEntity>>
    
    /**
     * Get only enabled repositories
     */
    fun getEnabled(): Flow<List<PluginRepositoryEntity>>
    
    /**
     * Get repository by URL
     */
    suspend fun getByUrl(url: String): PluginRepositoryEntity?
    
    /**
     * Get repository by ID
     */
    suspend fun getById(id: Long): PluginRepositoryEntity?
    
    /**
     * Add a new repository
     */
    suspend fun add(repository: PluginRepositoryEntity): Long
    
    /**
     * Update repository
     */
    suspend fun update(repository: PluginRepositoryEntity)
    
    /**
     * Toggle repository enabled state
     */
    suspend fun setEnabled(id: Long, enabled: Boolean)
    
    /**
     * Update plugin count after refresh
     */
    suspend fun updatePluginCount(id: Long, count: Int, lastUpdated: Long)
    
    /**
     * Update error state
     */
    suspend fun updateError(id: Long, error: String?, lastUpdated: Long)
    
    /**
     * Delete repository (cannot delete official)
     */
    suspend fun delete(id: Long)
    
    /**
     * Delete repository by URL (cannot delete official)
     */
    suspend fun deleteByUrl(url: String)
    
    /**
     * Initialize with default official repository if empty
     */
    suspend fun initializeDefaults()
}

/**
 * Plugin repository entity
 */
data class PluginRepositoryEntity(
    val id: Long = 0,
    val url: String,
    val name: String,
    val isEnabled: Boolean = true,
    val isOfficial: Boolean = false,
    val pluginCount: Int = 0,
    val lastUpdated: Long = 0,
    val lastError: String? = null,
    val createdAt: Long = 0
)
