package ireader.domain.models.quote

import androidx.compose.ui.graphics.Color

/**
 * Centralized color definitions for quote card styles.
 * Single source of truth to eliminate duplication across platform generators and UI components.
 */
object QuoteCardStyleColors {
    
    /**
     * Get gradient colors for a quote card style.
     * Returns a pair of (startColor, endColor) for vertical gradients.
     */
    fun getGradientColors(style: QuoteCardStyle): Pair<Color, Color> {
        return when (style) {
            QuoteCardStyle.GRADIENT_SUNSET -> Pair(
                Color(0xFFFF6B6B),
                Color(0xFFFFE66D)
            )
            QuoteCardStyle.GRADIENT_OCEAN -> Pair(
                Color(0xFF4ECDC4),
                Color(0xFF556270)
            )
            QuoteCardStyle.GRADIENT_FOREST -> Pair(
                Color(0xFF134E5E),
                Color(0xFF71B280)
            )
            QuoteCardStyle.GRADIENT_LAVENDER -> Pair(
                Color(0xFFDA22FF),
                Color(0xFF9733EE)
            )
            QuoteCardStyle.GRADIENT_MIDNIGHT -> Pair(
                Color(0xFF232526),
                Color(0xFF414345)
            )
            QuoteCardStyle.MINIMAL_LIGHT -> Pair(
                Color(0xFFF5F5F5),
                Color(0xFFFFFFFF)
            )
            QuoteCardStyle.MINIMAL_DARK -> Pair(
                Color(0xFF1A1A1A),
                Color(0xFF2D2D2D)
            )
            QuoteCardStyle.PAPER_TEXTURE -> Pair(
                Color(0xFFFFF8DC),
                Color(0xFFFAF0E6)
            )
            QuoteCardStyle.BOOK_COVER -> Pair(
                Color(0xFF8B4513),
                Color(0xFFD2691E)
            )
        }
    }
    
    /**
     * Get text color for a quote card style.
     * Returns white for dark backgrounds, black for light backgrounds.
     */
    fun getTextColor(style: QuoteCardStyle): Color {
        return when (style) {
            QuoteCardStyle.MINIMAL_LIGHT,
            QuoteCardStyle.PAPER_TEXTURE -> Color.Black
            else -> Color.White
        }
    }
}
