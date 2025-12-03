package ireader.data.chapterreport

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.ChapterReportRepository
import ireader.domain.models.entities.ChapterReport
import ireader.domain.models.entities.IssueCategory
import kotlinx.coroutines.flow.Flow
import ireader.domain.utils.extensions.currentTimeToLong

class ChapterReportRepositoryImpl(
    private val handler: DatabaseHandler
) : ChapterReportRepository {
    
    override fun subscribeAll(): Flow<List<ChapterReport>> {
        return handler.subscribeToList {
            chapterReportQueries.selectAll(::mapChapterReport)
        }
    }
    
    override fun subscribeByChapterId(chapterId: Long): Flow<List<ChapterReport>> {
        return handler.subscribeToList {
            chapterReportQueries.selectByChapterId(chapterId, ::mapChapterReport)
        }
    }
    
    override fun subscribeByBookId(bookId: Long): Flow<List<ChapterReport>> {
        return handler.subscribeToList {
            chapterReportQueries.selectByBookId(bookId, ::mapChapterReport)
        }
    }
    
    override fun subscribeUnresolved(): Flow<List<ChapterReport>> {
        return handler.subscribeToList {
            chapterReportQueries.selectUnresolved(::mapChapterReport)
        }
    }
    
    override suspend fun insert(
        chapterId: Long,
        bookId: Long,
        issueCategory: IssueCategory,
        description: String
    ): Long {
        handler.await(inTransaction = true) {
            chapterReportQueries.insert(
                chapterId = chapterId,
                bookId = bookId,
                issueCategory = issueCategory.name,
                description = description,
                timestamp = currentTimeToLong(),
                resolved = false
            )
        }
        return handler.awaitOneOrNull(inTransaction = false) {
            chapterReportQueries.selectAll(::mapChapterReport)
        }?.id ?: 0L
    }
    
    override suspend fun markAsResolved(reportId: Long) {
        handler.await(inTransaction = true) {
            chapterReportQueries.markAsResolved(reportId)
        }
    }
    
    override suspend fun delete(reportId: Long) {
        handler.await(inTransaction = true) {
            chapterReportQueries.deleteById(reportId)
        }
    }
    
    override suspend fun deleteByChapterId(chapterId: Long) {
        handler.await(inTransaction = true) {
            chapterReportQueries.deleteByChapterId(chapterId)
        }
    }
    
    private fun mapChapterReport(
        id: Long,
        chapterId: Long,
        bookId: Long,
        issueCategory: String,
        description: String,
        timestamp: Long,
        resolved: Boolean
    ): ChapterReport {
        return ChapterReport(
            id = id,
            chapterId = chapterId,
            bookId = bookId,
            issueCategory = IssueCategory.valueOf(issueCategory),
            description = description,
            timestamp = timestamp,
            resolved = resolved
        )
    }
}
