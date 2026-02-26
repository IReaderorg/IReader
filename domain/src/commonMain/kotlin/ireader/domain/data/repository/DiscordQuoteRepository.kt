package ireader.domain.data.repository

import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteCardStyle

/**
 * Repository for submitting quotes to Discord webhook
 */
interface DiscordQuoteRepository {
    /**
     * Submit a quote to Discord webhook with generated card image
     * 
     * @param quote The quote to submit
     * @param style The visual style for the quote card
     * @param username The username of the submitter
     * @return Result indicating success or failure
     */
    suspend fun submitQuote(
        quote: LocalQuote,
        style: QuoteCardStyle,
        username: String
    ): Result<Unit>
}
