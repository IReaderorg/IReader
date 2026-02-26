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
    return when (style) {
        QuoteCardStyle.GRADIENT_SUNSET -> Brush.verticalGradient(
            colors = listOf(Color(0xFFFF6B6B), Color(0xFFFFE66D))
        )
        QuoteCardStyle.GRADIENT_OCEAN -> Brush.verticalGradient(
            colors = listOf(Color(0xFF4ECDC4), Color(0xFF556270))
        )
        QuoteCardStyle.GRADIENT_FOREST -> Brush.verticalGradient(
            colors = listOf(Color(0xFF134E5E), Color(0xFF71B280))
        )
        QuoteCardStyle.GRADIENT_LAVENDER -> Brush.verticalGradient(
            colors = listOf(Color(0xFFDA22FF), Color(0xFF9733EE))
        )
        QuoteCardStyle.GRADIENT_MIDNIGHT -> Brush.verticalGradient(
            colors = listOf(Color(0xFF232526), Color(0xFF414345))
        )
        QuoteCardStyle.MINIMAL_LIGHT -> Brush.verticalGradient(
            colors = listOf(Color(0xFFF5F5F5), Color(0xFFFFFFFF))
        )
        QuoteCardStyle.MINIMAL_DARK -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1A1A1A), Color(0xFF2D2D2D))
        )
        QuoteCardStyle.PAPER_TEXTURE -> Brush.verticalGradient(
            colors = listOf(Color(0xFFFFF8DC), Color(0xFFFAF0E6))
        )
        QuoteCardStyle.BOOK_COVER -> Brush.verticalGradient(
            colors = listOf(Color(0xFF8B4513), Color(0xFFD2691E))
        )
    }
}

private fun getStyleTextColor(style: QuoteCardStyle): Color {
    return when (style) {
        QuoteCardStyle.MINIMAL_LIGHT,
        QuoteCardStyle.PAPER_TEXTURE -> Color.Black
        else -> Color.White
    }
}
