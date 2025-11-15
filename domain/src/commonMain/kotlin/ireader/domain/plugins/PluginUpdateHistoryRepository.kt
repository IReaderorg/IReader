package ireader.domain.plugins

/**
 * Repository interface for plugin update history
 * Requirements: 12.5
 */
interface PluginUpdateHistoryRepository {
    
    /**
     * Save an update history record
     * Requirements: 12.5
     */
    suspend fun saveUpdateHistory(history: PluginUpdateHistory)
    
    /**
     * Get update history for a specific plugin
     * Requirements: 12.5
     */
    suspend fun getUpdateHistory(pluginId: String): List<PluginUpdateHistory>
    
    /**
     * Get all update history across all plugins
     * Requirements: 12.5
     */
    suspend fun getAllUpdateHistory(): List<PluginUpdateHistory>
    
    /**
     * Update the last history record for a plugin with success status
     * Requirements: 12.5
     */
    suspend fun updateLastHistorySuccess(
        pluginId: String,
        toVersion: String,
        toVersionCode: Int,
        success: Boolean
    )
    
    /**
     * Delete update history for a plugin
     * Called when a plugin is uninstalled
     * Requirements: 12.5
     */
    suspend fun deleteUpdateHistory(pluginId: String)
    
    /**
     * Get the most recent update for a plugin
     * Requirements: 12.5
     */
    suspend fun getLatestUpdate(pluginId: String): PluginUpdateHistory?
}

/**
 * Record of a plugin update
 * Requirements: 12.5
 */
data class PluginUpdateHistory(
    val id: Long = 0,
    val pluginId: String,
    val fromVersion: String,
    val fromVersionCode: Int,
    val toVersion: String,
    val toVersionCode: Int,
    val updateDate: Long,
    val success: Boolean,
    val errorMessage: String? = null
)
