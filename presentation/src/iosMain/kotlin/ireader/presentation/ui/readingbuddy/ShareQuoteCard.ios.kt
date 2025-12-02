package ireader.presentation.ui.readingbuddy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ireader.domain.models.quote.Quote
import ireader.domain.models.quote.QuoteCardStyle

actual class QuoteCardSharer {
    actual suspend fun shareQuoteCard(
        quote: Quote,
        style: QuoteCardStyle,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // iOS implementation - for now just show text
        // TODO: Implement native iOS sharing with UIActivityViewController
        onError("Image sharing not yet implemented for iOS. Quote text copied.")
    }
}

@Composable
actual fun rememberQuoteCardSharer(): QuoteCardSharer {
    return remember { QuoteCardSharer() }
}
