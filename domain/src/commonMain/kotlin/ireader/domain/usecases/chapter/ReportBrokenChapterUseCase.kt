package ireader.domain.usecases.chapter

import ireader.domain.data.repository.ChapterReportRepository
import ireader.domain.models.entities.IssueCategory

class ReportBrokenChapterUseCase(
    private val chapterReportRepository: ChapterReportRepository
) {
    /**
     * Report a broken chapter with the specified details.
     * 
     * @param chapterId The ID of the chapter being reported
     * @param bookId The ID of the book containing the chapter
     * @param sourceId The ID of the source (not stored in entity, can be derived from book)
     * @param reason The reason for the report (mapped to IssueCategory)
     * @param description Additional details about the issue
     * @return Result<Unit> indicating success or failure
     */
    suspend operator fun invoke(
        chapterId: Long,
        bookId: Long,
        sourceId: Long = 0, // Optional parameter for API compatibility
        reason: String,
        description: String
    ): Result<Unit> {
        return try {
            // Map reason string to IssueCategory enum
            val issueCategory = when (reason.lowercase()) {
                "missing content", "missing_content" -> IssueCategory.MISSING_CONTENT
                "incorrect order", "incorrect content", "incorrect_content" -> IssueCategory.INCORRECT_CONTENT
                "formatting issues", "formatting_issues" -> IssueCategory.FORMATTING_ISSUES
                "translation errors", "translation_errors" -> IssueCategory.TRANSLATION_ERRORS
                "duplicate content", "duplicate_content" -> IssueCategory.DUPLICATE_CONTENT
                else -> IssueCategory.OTHER
            }
            
            chapterReportRepository.insert(
                chapterId = chapterId,
                bookId = bookId,
                issueCategory = issueCategory,
                description = description
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Legacy execute method for backward compatibility.
     * 
     * @param chapterId The ID of the chapter being reported
     * @param bookId The ID of the book containing the chapter
     * @param issueCategory The category of the issue
     * @param description Additional details about the issue
     * @return Result<Long> containing the report ID on success
     */
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
