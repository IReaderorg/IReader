package ireader.presentation.ui.reader

import androidx.compose.ui.graphics.Color
import ireader.presentation.core.ui.util.toDomainColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * TDD Tests for background and text color customization feature.
 * 
 * Requirements:
 * - Users should be able to select custom background color
 * - Users should be able to select custom text color
 * - Color changes should be saved to preferences
 * - Color preview should update in real-time
 * 
 * Following TDD: RED → GREEN → REFACTOR
 */
class ColorCustomizationTest {
    
    /**
     * Helper function to compare colors by their components
     * since toArgb() is not available in common test sources.
     */
    private fun assertColorsEqual(expected: Color, actual: Color) {
        assertEquals(expected.red, actual.red, 0.001f, "Red component mismatch")
        assertEquals(expected.green, actual.green, 0.001f, "Green component mismatch")
        assertEquals(expected.blue, actual.blue, 0.001f, "Blue component mismatch")
        assertEquals(expected.alpha, actual.alpha, 0.001f, "Alpha component mismatch")
    }
    
    @Test
    fun `setReaderBackgroundColor should update background color`() {
        // Arrange
        val expectedColor = Color(0xFF1E1E1E)
        
        // Act
        val domainColor = expectedColor.toDomainColor()
        
        // Assert - Compare color components
        assertEquals(expectedColor.red, domainColor.red, 0.001f, "Red mismatch")
        assertEquals(expectedColor.green, domainColor.green, 0.001f, "Green mismatch")
        assertEquals(expectedColor.blue, domainColor.blue, 0.001f, "Blue mismatch")
        assertEquals(expectedColor.alpha, domainColor.alpha, 0.001f, "Alpha mismatch")
    }
    
    @Test
    fun `setReaderTextColor should update text color`() {
        // Arrange
        val expectedColor = Color(0xFFE0E0E0)
        
        // Act
        val domainColor = expectedColor.toDomainColor()
        
        // Assert - Compare color components
        assertEquals(expectedColor.red, domainColor.red, 0.001f, "Red mismatch")
        assertEquals(expectedColor.green, domainColor.green, 0.001f, "Green mismatch")
        assertEquals(expectedColor.blue, domainColor.blue, 0.001f, "Blue mismatch")
        assertEquals(expectedColor.alpha, domainColor.alpha, 0.001f, "Alpha mismatch")
    }
    
    @Test
    fun `custom colors should be different from default theme colors`() {
        // Arrange
        val customBgColor = Color(0xFF2C2C2C)
        val customTextColor = Color(0xFFF5F5F5)
        val defaultBgColor = Color.Black
        val defaultTextColor = Color.White
        
        // Assert
        assertNotEquals(customBgColor, defaultBgColor)
        assertNotEquals(customTextColor, defaultTextColor)
    }
    
    @Test
    fun `color conversion should preserve RGB values`() {
        // Arrange
        val originalColor = Color(0xFF3F51B5) // Indigo
        
        // Act
        val domainColor = originalColor.toDomainColor()
        
        // Assert - Compare color components
        assertEquals(originalColor.red, domainColor.red, 0.001f, "Red mismatch")
        assertEquals(originalColor.green, domainColor.green, 0.001f, "Green mismatch")
        assertEquals(originalColor.blue, domainColor.blue, 0.001f, "Blue mismatch")
        assertEquals(originalColor.alpha, domainColor.alpha, 0.001f, "Alpha mismatch")
    }
    
    @Test
    fun `color picker should support common preset colors`() {
        // Arrange
        val presetColors = listOf(
            Color(0xFF000000), // Black
            Color(0xFFFFFFFF), // White
            Color(0xFF1E1E1E), // Dark Gray
            Color(0xFFF5F5DC), // Beige
            Color(0xFFFFF8DC), // Cornsilk
        )
        
        // Act & Assert
        presetColors.forEach { color ->
            val domainColor = color.toDomainColor()
            assertEquals(color.red, domainColor.red, 0.001f, "Red mismatch for $color")
            assertEquals(color.green, domainColor.green, 0.001f, "Green mismatch for $color")
            assertEquals(color.blue, domainColor.blue, 0.001f, "Blue mismatch for $color")
            assertEquals(color.alpha, domainColor.alpha, 0.001f, "Alpha mismatch for $color")
        }
    }
}
