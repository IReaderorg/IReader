package ireader.domain.data.repository

import ireader.domain.models.entities.ChapterReport
import ireader.domain.models.entities.IssueCategory
import kotlinx.coroutines.flow.Flow

interface ChapterReportRepository {
    /**
     * Subscribe to all chapter reports
     */
    fun subscribeAll(): Flow<List<ChapterReport>>
    
    /**
     * Subscribe to reports for a specific chapter
     */
    fun subscribeByChapterId(chapterId: Long): Flow<List<ChapterReport>>
    
    /**
     * Subscribe to reports for a specific book
     */
    fun subscribeByBookId(bookId: Long): Flow<List<ChapterReport>>
    
    /**
     * Subscribe to unresolved reports
     */
    fun subscribeUnresolved(): Flow<List<ChapterReport>>
    
    /**
     * Insert a new chapter report
     */
    suspend fun insert(
        chapterId: Long,
        bookId: Long,
        issueCategory: IssueCategory,
        description: String
    ): Long
    
    /**
     * Mark a report as resolved
     */
    suspend fun markAsResolved(reportId: Long)
    
    /**
     * Delete a report
     */
    suspend fun delete(reportId: Long)
    
    /**
     * Delete all reports for a chapter
     */
    suspend fun deleteByChapterId(chapterId: Long)
}
