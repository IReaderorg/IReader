package ireader.presentation.ui.readingbuddy

import androidx.compose.runtime.Composable
import ireader.domain.models.quote.Quote
import ireader.domain.models.quote.QuoteCardStyle

/**
 * Platform-specific share functionality for quote cards
 */
expect class QuoteCardSharer {
    /**
     * Share a quote card as an image to any platform
     * @param quote The quote to share
     * @param style The card style to use
     * @param onSuccess Called when share is successful
     * @param onError Called when share fails
     */
    suspend fun shareQuoteCard(
        quote: Quote,
        style: QuoteCardStyle,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
}

/**
 * Composable to get platform-specific QuoteCardSharer
 */
@Composable
expect fun rememberQuoteCardSharer(): QuoteCardSharer
