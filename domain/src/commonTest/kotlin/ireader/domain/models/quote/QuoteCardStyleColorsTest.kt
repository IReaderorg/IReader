package ireader.domain.models.quote

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests for QuoteCardStyleColors to ensure consistent color definitions
 * across all platforms and UI components.
 */
class QuoteCardStyleColorsTest {
    
    @Test
    fun `getGradientColors should return sunset colors for GRADIENT_SUNSET`() {
        // Arrange
        val style = QuoteCardStyle.GRADIENT_SUNSET
        
        // Act
        val (startColor, endColor) = QuoteCardStyleColors.getGradientColors(style)
        
        // Assert
        assertEquals(Color(0xFFFF6B6B), startColor)
        assertEquals(Color(0xFFFFE66D), endColor)
    }
    
    @Test
    fun `getGradientColors should return ocean colors for GRADIENT_OCEAN`() {
        // Arrange
        val style = QuoteCardStyle.GRADIENT_OCEAN
        
        // Act
        val (startColor, endColor) = QuoteCardStyleColors.getGradientColors(style)
        
        // Assert
        assertEquals(Color(0xFF4ECDC4), startColor)
        assertEquals(Color(0xFF556270), endColor)
    }
    
    @Test
    fun `getGradientColors should return forest colors for GRADIENT_FOREST`() {
        // Arrange
        val style = QuoteCardStyle.GRADIENT_FOREST
        
        // Act
        val (startColor, endColor) = QuoteCardStyleColors.getGradientColors(style)
        
        // Assert
        assertEquals(Color(0xFF134E5E), startColor)
        assertEquals(Color(0xFF71B280), endColor)
    }
    
    @Test
    fun `getGradientColors should return lavender colors for GRADIENT_LAVENDER`() {
        // Arrange
        val style = QuoteCardStyle.GRADIENT_LAVENDER
        
        // Act
        val (startColor, endColor) = QuoteCardStyleColors.getGradientColors(style)
        
        // Assert
        assertEquals(Color(0xFFDA22FF), startColor)
        assertEquals(Color(0xFF9733EE), endColor)
    }
    
    @Test
    fun `getGradientColors should return midnight colors for GRADIENT_MIDNIGHT`() {
        // Arrange
        val style = QuoteCardStyle.GRADIENT_MIDNIGHT
        
        // Act
        val (startColor, endColor) = QuoteCardStyleColors.getGradientColors(style)
        
        // Assert
        assertEquals(Color(0xFF232526), startColor)
        assertEquals(Color(0xFF414345), endColor)
    }
    
    @Test
    fun `getGradientColors should return light colors for MINIMAL_LIGHT`() {
        // Arrange
        val style = QuoteCardStyle.MINIMAL_LIGHT
        
        // Act
        val (startColor, endColor) = QuoteCardStyleColors.getGradientColors(style)
        
        // Assert
        assertEquals(Color(0xFFF5F5F5), startColor)
        assertEquals(Color(0xFFFFFFFF), endColor)
    }
    
    @Test
    fun `getGradientColors should return dark colors for MINIMAL_DARK`() {
        // Arrange
        val style = QuoteCardStyle.MINIMAL_DARK
        
        // Act
        val (startColor, endColor) = QuoteCardStyleColors.getGradientColors(style)
        
        // Assert
        assertEquals(Color(0xFF1A1A1A), startColor)
        assertEquals(Color(0xFF2D2D2D), endColor)
    }
    
    @Test
    fun `getGradientColors should return paper colors for PAPER_TEXTURE`() {
        // Arrange
        val style = QuoteCardStyle.PAPER_TEXTURE
        
        // Act
        val (startColor, endColor) = QuoteCardStyleColors.getGradientColors(style)
        
        // Assert
        assertEquals(Color(0xFFFFF8DC), startColor)
        assertEquals(Color(0xFFFAF0E6), endColor)
    }
    
    @Test
    fun `getGradientColors should return book cover colors for BOOK_COVER`() {
        // Arrange
        val style = QuoteCardStyle.BOOK_COVER
        
        // Act
        val (startColor, endColor) = QuoteCardStyleColors.getGradientColors(style)
        
        // Assert
        assertEquals(Color(0xFF8B4513), startColor)
        assertEquals(Color(0xFFD2691E), endColor)
    }
    
    @Test
    fun `getTextColor should return white for dark backgrounds`() {
        // Arrange
        val darkStyles = listOf(
            QuoteCardStyle.GRADIENT_SUNSET,
            QuoteCardStyle.GRADIENT_OCEAN,
            QuoteCardStyle.GRADIENT_FOREST,
            QuoteCardStyle.GRADIENT_LAVENDER,
            QuoteCardStyle.GRADIENT_MIDNIGHT,
            QuoteCardStyle.MINIMAL_DARK,
            QuoteCardStyle.BOOK_COVER
        )
        
        // Act & Assert
        darkStyles.forEach { style ->
            val textColor = QuoteCardStyleColors.getTextColor(style)
            assertEquals(Color.White, textColor, "Expected white text for $style")
        }
    }
    
    @Test
    fun `getTextColor should return black for light backgrounds`() {
        // Arrange
        val lightStyles = listOf(
            QuoteCardStyle.MINIMAL_LIGHT,
            QuoteCardStyle.PAPER_TEXTURE
        )
        
        // Act & Assert
        lightStyles.forEach { style ->
            val textColor = QuoteCardStyleColors.getTextColor(style)
            assertEquals(Color.Black, textColor, "Expected black text for $style")
        }
    }
    
    @Test
    fun `all QuoteCardStyle values should have gradient colors defined`() {
        // Arrange & Act & Assert
        QuoteCardStyle.entries.forEach { style ->
            val (startColor, endColor) = QuoteCardStyleColors.getGradientColors(style)
            assertNotNull(startColor, "Start color should not be null for $style")
            assertNotNull(endColor, "End color should not be null for $style")
        }
    }
    
    @Test
    fun `all QuoteCardStyle values should have text color defined`() {
        // Arrange & Act & Assert
        QuoteCardStyle.entries.forEach { style ->
            val textColor = QuoteCardStyleColors.getTextColor(style)
            assertNotNull(textColor, "Text color should not be null for $style")
        }
    }
}
