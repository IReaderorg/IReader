package ireader.domain.models.quote

import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.serialization.Serializable

/**
 * Local quote stored on device - unlimited length, not synced to Supabase
 */
@Serializable
data class LocalQuote(
    val id: Long = 0,
    val text: String,
    val bookId: Long,
    val bookTitle: String,
    val chapterTitle: String,
    val chapterNumber: Int? = null,
    val author: String? = null,
    val createdAt: Long = currentTimeToLong(),
    val hasContextBackup: Boolean = false
)

/**
 * Chapter content backup for quote context
 */
@Serializable
data class QuoteContext(
    val id: Long = 0,
    val quoteId: Long,
    val chapterId: Long,
    val chapterTitle: String,
    val content: String
)

/**
 * Validation result for sharing quote to community
 */
data class ShareValidation(
    val canShare: Boolean,
    val currentLength: Int,
    val maxLength: Int = 1000,
    val minLength: Int = 10,
    val reason: String? = null
) {
    val needsTruncation: Boolean get() = currentLength > maxLength
    val tooShort: Boolean get() = currentLength < minLength
}

/**
 * Parameters passed from reader to quote creation screen
 */
@Serializable
data class QuoteCreationParams(
    val bookId: Long,
    val bookTitle: String,
    val chapterTitle: String,
    val chapterNumber: Int? = null,
    val author: String? = null,
    val currentChapterId: Long? = null,
    val prevChapterId: Long? = null,
    val nextChapterId: Long? = null
)
