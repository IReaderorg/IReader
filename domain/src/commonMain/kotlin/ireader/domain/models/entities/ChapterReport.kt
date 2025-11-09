package ireader.domain.models.entities

import kotlinx.serialization.Serializable

/**
 * Represents a user report for a broken or problematic chapter
 */
@Serializable
data class ChapterReport(
    val id: Long = 0,
    val chapterId: Long,
    val bookId: Long,
    val issueCategory: IssueCategory,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val resolved: Boolean = false
)

/**
 * Categories of issues that can be reported for a chapter
 */
enum class IssueCategory {
    MISSING_CONTENT,
    INCORRECT_CONTENT,
    FORMATTING_ISSUES,
    TRANSLATION_ERRORS,
    DUPLICATE_CONTENT,
    OTHER
}
