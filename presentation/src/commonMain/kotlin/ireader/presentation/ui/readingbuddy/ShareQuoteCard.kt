package ireader.presentation.ui.readingbuddy

import androidx.compose.runtime.Composable
import ireader.domain.models.quote.Quote
import ireader.domain.models.quote.QuoteCardStyle

/**
 * Platform-specific quote card sharer
 */
expect class QuoteCardSharer {
    suspend fun shareQuoteCard(
        quote: Quote,
        style: QuoteCardStyle,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
}

/**
 * Remember a QuoteCardSharer instance
 */
@Composable
expect fun rememberQuoteCardSharer(): QuoteCardSharer
