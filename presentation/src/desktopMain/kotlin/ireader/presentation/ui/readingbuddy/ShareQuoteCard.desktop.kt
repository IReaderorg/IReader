package ireader.presentation.ui.readingbuddy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ireader.domain.models.quote.Quote
import ireader.domain.models.quote.QuoteCardStyle
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual class QuoteCardSharer {
    
    actual suspend fun shareQuoteCard(
        quote: Quote,
        style: QuoteCardStyle,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val shareText = buildShareText(quote)
            
            // Copy to clipboard on desktop
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val selection = StringSelection(shareText)
            clipboard.setContents(selection, selection)
            
            onSuccess()
        } catch (e: Exception) {
            onError("Failed to copy quote: ${e.message}")
        }
    }
    
    private fun buildShareText(quote: Quote): String {
        return buildString {
            append("\"")
            append(quote.text)
            append("\"")
            append("\n\n")
            
            if (quote.author.isNotBlank()) {
                append("— ")
                append(quote.author)
            }
            
            if (quote.bookTitle.isNotBlank()) {
                if (quote.author.isNotBlank()) {
                    append(", ")
                } else {
                    append("— ")
                }
                append(quote.bookTitle)
            }
            
            if (quote.submitterUsername.isNotBlank()) {
                append("\n")
                append("Shared by ")
                append(quote.submitterUsername)
            }
            
            append("\n\n")
            append("📚 Shared via IReader")
        }
    }
}

@Composable
actual fun rememberQuoteCardSharer(): QuoteCardSharer {
    return remember { QuoteCardSharer() }
}
