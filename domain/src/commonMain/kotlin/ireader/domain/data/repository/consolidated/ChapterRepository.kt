package ireader.domain.data.repository.consolidated

import ireader.domain.models.entities.Chapter
import ireader.domain.models.updates.ChapterUpdate
import kotlinx.coroutines.flow.Flow

/**
 * Consolidated ChapterRepository following Mihon's focused, single-responsibility pattern.
 * 
 * This repository provides essential chapter operations with both suspend functions
 * and Flow-based reactive queries following Mihon's subscribeToOne and subscribeToList patterns.
 */
interface ChapterRepository {
    
    // Single chapter operations
    suspend fun getChapterById(id: Long): Chapter?
    fun getChapterByIdAsFlow(id: Long): Flow<Chapter?>
    
    // Book-related chapter operations
    suspend fun getChaptersByBookId(bookId: Long): List<Chapter>
    fun getChaptersByBookIdAsFlow(bookId: Long): Flow<List<Chapter>>
    
    // Reading progress operations
    suspend fun getLastReadChapter(bookId: Long): Chapter?
    fun getLastReadChapterAsFlow(bookId: Long): Flow<Chapter?>
    
    // Insertion operations
    suspend fun addAll(chapters: List<Chapter>): List<Chapter>
    
    // Update operations following Mihon's pattern
    suspend fun update(update: ChapterUpdate): Boolean
    suspend fun updateAll(updates: List<ChapterUpdate>): Boolean
    
    // Deletion operations
    suspend fun removeChaptersWithIds(chapterIds: List<Long>): Boolean
    suspend fun removeChaptersByBookId(bookId: Long): Boolean
}