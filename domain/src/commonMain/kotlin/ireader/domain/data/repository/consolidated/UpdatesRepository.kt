package ireader.domain.data.repository.consolidated

import ireader.domain.models.entities.UpdatesWithRelations
import kotlinx.coroutines.flow.Flow

/**
 * Consolidated UpdatesRepository following Mihon's focused, single-responsibility pattern.
 * 
 * This repository provides essential update tracking operations for monitoring
 * new chapters and content updates.
 */
interface UpdatesRepository {
    
    // Updates retrieval
    suspend fun getAllUpdates(after: Long = 0): List<UpdatesWithRelations>
    fun getAllUpdatesAsFlow(after: Long = 0): Flow<List<UpdatesWithRelations>>
    
    // Recent updates
    suspend fun getRecentUpdates(limit: Int = 50): List<UpdatesWithRelations>
    fun getRecentUpdatesAsFlow(limit: Int = 50): Flow<List<UpdatesWithRelations>>
    
    // Updates by book
    suspend fun getUpdatesByBookId(bookId: Long): List<UpdatesWithRelations>
    fun getUpdatesByBookIdAsFlow(bookId: Long): Flow<List<UpdatesWithRelations>>
    
    // Update management
    suspend fun insertUpdate(bookId: Long, chapterId: Long): Boolean
    suspend fun markUpdateRead(updateId: Long): Boolean
    suspend fun markUpdatesRead(updateIds: List<Long>): Boolean
    
    // Cleanup operations
    suspend fun deleteUpdate(updateId: Long): Boolean
    suspend fun deleteUpdatesByBookId(bookId: Long): Boolean
    suspend fun deleteOldUpdates(olderThanDays: Int): Boolean
    suspend fun clearAllUpdates(): Boolean
}