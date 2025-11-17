package ireader.domain.data.repository.consolidated

import ireader.domain.models.entities.ExtensionSource
import kotlinx.coroutines.flow.Flow

/**
 * Consolidated SourceRepository following Mihon's focused, single-responsibility pattern.
 * 
 * This repository provides essential source management operations with proper
 * extension handling and source configuration capabilities.
 */
interface SourceRepository {
    
    // Source retrieval
    suspend fun getAllSources(): List<ExtensionSource>
    fun getAllSourcesAsFlow(): Flow<List<ExtensionSource>>
    
    suspend fun getSourceById(id: Long): ExtensionSource?
    suspend fun getEnabledSources(): List<ExtensionSource>
    fun getEnabledSourcesAsFlow(): Flow<List<ExtensionSource>>
    
    // Source management
    suspend fun enableSource(sourceId: Long): Boolean
    suspend fun disableSource(sourceId: Long): Boolean
    suspend fun toggleSource(sourceId: Long): Boolean
    
    // Source configuration
    suspend fun updateSourcePreferences(sourceId: Long, preferences: Map<String, Any>): Boolean
    suspend fun resetSourcePreferences(sourceId: Long): Boolean
    
    // Source statistics
    suspend fun getSourceBookCount(sourceId: Long): Int
    suspend fun getSourceLastUsed(sourceId: Long): Long?
    
    // Bulk operations
    suspend fun enableSources(sourceIds: List<Long>): Boolean
    suspend fun disableSources(sourceIds: List<Long>): Boolean
}