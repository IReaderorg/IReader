package ireader.presentation.ui.quote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.domain.models.quote.QuoteCardStyle
import ireader.domain.models.quote.QuoteCardStyleColors

/**
 * Live preview of quote with selected style.
 * Updates in real-time as user types.
 */
@Composable
fun QuoteLivePreview(
    quoteText: String,
    bookTitle: String,
    author: String,
    style: QuoteCardStyle,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .background(getStyleBackground(style)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Quote text
            if (quoteText.isNotBlank()) {
                Text(
                    text = "\"$quoteText\"",
                    style = MaterialTheme.typography.headlineSmall,
                    color = getStyleTextColor(style),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "Your quote will appear here...",
                    style = MaterialTheme.typography.headlineSmall,
                    color = getStyleTextColor(style).copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Book title
            if (bookTitle.isNotBlank()) {
                Text(
                    text = bookTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = getStyleTextColor(style).copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
            
            // Author
            if (author.isNotBlank()) {
                Text(
                    text = "by $author",
                    style = MaterialTheme.typography.bodyMedium,
                    color = getStyleTextColor(style).copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun getStyleBackground(style: QuoteCardStyle): Brush {
    val (startColor, endColor) = QuoteCardStyleColors.getGradientColors(style)
    return Brush.verticalGradient(colors = listOf(startColor, endColor))
}

private fun getStyleTextColor(style: QuoteCardStyle): Color {
    return QuoteCardStyleColors.getTextColor(style)
}
