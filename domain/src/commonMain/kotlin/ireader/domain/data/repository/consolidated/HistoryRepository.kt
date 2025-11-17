package ireader.domain.data.repository.consolidated

import ireader.domain.models.entities.History
import kotlinx.coroutines.flow.Flow

/**
 * Consolidated HistoryRepository following Mihon's focused, single-responsibility pattern.
 * 
 * This repository provides essential reading history operations with proper
 * tracking and cleanup capabilities.
 */
interface HistoryRepository {
    
    // History retrieval
    suspend fun getHistoryById(id: Long): History?
    suspend fun getAllHistory(): List<History>
    fun getAllHistoryAsFlow(): Flow<List<History>>
    
    // History by book
    suspend fun getHistoryByBookId(bookId: Long): List<History>
    fun getHistoryByBookIdAsFlow(bookId: Long): Flow<List<History>>
    
    // Recent history
    suspend fun getRecentHistory(limit: Int = 50): List<History>
    fun getRecentHistoryAsFlow(limit: Int = 50): Flow<List<History>>
    
    // History management
    suspend fun insertHistory(history: History): Boolean
    suspend fun updateLastRead(bookId: Long, chapterId: Long, readAt: Long): Boolean
    
    // Cleanup operations
    suspend fun deleteHistory(historyId: Long): Boolean
    suspend fun deleteHistoryByBookId(bookId: Long): Boolean
    suspend fun deleteOldHistory(olderThanDays: Int): Boolean
    suspend fun clearAllHistory(): Boolean
}