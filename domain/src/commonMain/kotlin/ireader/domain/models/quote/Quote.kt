package ireader.domain.models.quote

import kotlinx.serialization.Serializable
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Represents a quote from a book that can be shared
 */
@Serializable
data class Quote(
    val id: String = "",
    val text: String,
    val bookTitle: String,
    val author: String = "",
    val chapterTitle: String = "",
    val submitterId: String = "",
    val submitterUsername: String = "",
    val likesCount: Int = 0,
    val isLikedByUser: Boolean = false,
    val status: QuoteStatus = QuoteStatus.APPROVED,
    val submittedAt: Long = currentTimeToLong(),
    val isFeatured: Boolean = false
)

/**
 * Quote approval status
 */
enum class QuoteStatus {
    PENDING,
    APPROVED,
    REJECTED
}

/**
 * Daily quote with additional metadata
 */
@Serializable
data class DailyQuote(
    val quote: Quote,
    val dateShown: Long = currentTimeToLong(),
    val cardStyle: QuoteCardStyle = QuoteCardStyle.GRADIENT_SUNSET
)

/**
 * Available card styles for shareable quote cards
 */
enum class QuoteCardStyle(val displayName: String) {
    GRADIENT_SUNSET("Sunset"),
    GRADIENT_OCEAN("Ocean"),
    GRADIENT_FOREST("Forest"),
    GRADIENT_LAVENDER("Lavender"),
    GRADIENT_MIDNIGHT("Midnight"),
    MINIMAL_LIGHT("Minimal Light"),
    MINIMAL_DARK("Minimal Dark"),
    PAPER_TEXTURE("Paper"),
    BOOK_COVER("Book Cover")
}

/**
 * Request to submit a new quote
 */
@Serializable
data class SubmitQuoteRequest(
    val quoteText: String,
    val bookTitle: String,
    val author: String = "",
    val chapterTitle: String = ""
)
