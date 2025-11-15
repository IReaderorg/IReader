package ireader.domain.plugins

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory implementation of PluginUpdateHistoryRepository
 * This is a simple implementation for testing and development
 * Production implementation should use actual database storage
 * Requirements: 12.5
 */
class InMemoryPluginUpdateHistoryRepository : PluginUpdateHistoryRepository {
    
    private val history = mutableListOf<PluginUpdateHistory>()
    private val mutex = Mutex()
    private var nextId = 1L
    
    override suspend fun saveUpdateHistory(history: PluginUpdateHistory) {
        mutex.withLock {
            val historyWithId = if (history.id == 0L) {
                history.copy(id = nextId++)
            } else {
                history
            }
            this.history.add(historyWithId)
        }
    }
    
    override suspend fun getUpdateHistory(pluginId: String): List<PluginUpdateHistory> {
        return mutex.withLock {
            history.filter { it.pluginId == pluginId }
                .sortedByDescending { it.updateDate }
        }
    }
    
    override suspend fun getAllUpdateHistory(): List<PluginUpdateHistory> {
        return mutex.withLock {
            history.sortedByDescending { it.updateDate }
        }
    }
    
    override suspend fun updateLastHistorySuccess(
        pluginId: String,
        toVersion: String,
        toVersionCode: Int,
        success: Boolean
    ) {
        mutex.withLock {
            val lastHistory = history
                .filter { it.pluginId == pluginId }
                .maxByOrNull { it.updateDate }
            
            if (lastHistory != null) {
                val index = history.indexOf(lastHistory)
                history[index] = lastHistory.copy(
                    toVersion = toVersion,
                    toVersionCode = toVersionCode,
                    success = success
                )
            }
        }
    }
    
    override suspend fun deleteUpdateHistory(pluginId: String) {
        mutex.withLock {
            history.removeAll { it.pluginId == pluginId }
        }
    }
    
    override suspend fun getLatestUpdate(pluginId: String): PluginUpdateHistory? {
        return mutex.withLock {
            history.filter { it.pluginId == pluginId }
                .maxByOrNull { it.updateDate }
        }
    }
}
