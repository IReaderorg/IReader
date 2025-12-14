package ireader.domain.usersource.repository

import ireader.domain.usersource.model.UserSource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user-defined sources.
 */
interface UserSourceRepository {
    
    /** Get all sources as flow */
    fun getAllAsFlow(): Flow<List<UserSource>>
    
    /** Get all sources */
    suspend fun getAll(): List<UserSource>
    
    /** Get enabled sources only */
    suspend fun getEnabled(): List<UserSource>
    
    /** Get source by URL */
    suspend fun getByUrl(sourceUrl: String): UserSource?
    
    /** Get source by ID */
    suspend fun getById(sourceId: Long): UserSource?
    
    /** Insert or update a source */
    suspend fun upsert(source: UserSource)
    
    /** Insert or update multiple sources */
    suspend fun upsertAll(sources: List<UserSource>)
    
    /** Delete a source by URL */
    suspend fun delete(sourceUrl: String)
    
    /** Delete a source by ID */
    suspend fun deleteById(sourceId: Long)
    
    /** Delete all sources */
    suspend fun deleteAll()
    
    /** Set source enabled state */
    suspend fun setEnabled(sourceUrl: String, enabled: Boolean)
    
    /** Update source order */
    suspend fun updateOrder(sourceUrl: String, newOrder: Int)
    
    /** Get sources by group */
    suspend fun getByGroup(group: String): List<UserSource>
    
    /** Get all unique groups */
    suspend fun getGroups(): List<String>
    
    /** Export all sources to JSON */
    suspend fun exportToJson(): String
    
    /** Import sources from JSON */
    suspend fun importFromJson(json: String): Result<Int>
}
