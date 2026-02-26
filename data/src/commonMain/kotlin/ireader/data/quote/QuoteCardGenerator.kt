package ireader.data.quote

import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteCardStyle

/**
 * Platform-specific quote card image generator.
 * Renders quote text with styled background as PNG image.
 */
interface QuoteCardGenerator {
    /**
     * Generate a quote card image as PNG bytes.
     * 
     * @param quote The quote to render
     * @param style The visual style to use
     * @return PNG image bytes (1080x1920 for Instagram story format)
     */
    suspend fun generateQuoteCard(quote: LocalQuote, style: QuoteCardStyle): ByteArray
}
