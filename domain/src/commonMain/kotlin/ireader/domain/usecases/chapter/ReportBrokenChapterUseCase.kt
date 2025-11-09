package ireader.domain.usecases.chapter

import ireader.domain.data.repository.ChapterReportRepository
import ireader.domain.models.entities.IssueCategory

class ReportBrokenChapterUseCase(
    private val chapterReportRepository: ChapterReportRepository
) {
    suspend fun execute(
        chapterId: Long,
        bookId: Long,
        issueCategory: IssueCategory,
        description: String
    ): Result<Long> {
        return try {
            val reportId = chapterReportRepository.insert(
                chapterId = chapterId,
                bookId = bookId,
                issueCategory = issueCategory,
                description = description
            )
            Result.success(reportId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
