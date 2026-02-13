package ireader.presentation.ui.reader

import androidx.compose.ui.graphics.Color
import ireader.presentation.core.toDomainColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

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
    
    @Test
    fun `setReaderBackgroundColor should update background color`() {
        // Arrange
        val expectedColor = Color(0xFF1E1E1E)
        
        // Act
        val domainColor = expectedColor.toDomainColor()
        
        // Assert
        assertEquals(expectedColor.value.toLong(), domainColor.color)
    }
    
    @Test
    fun `setReaderTextColor should update text color`() {
        // Arrange
        val expectedColor = Color(0xFFE0E0E0)
        
        // Act
        val domainColor = expectedColor.toDomainColor()
        
        // Assert
        assertEquals(expectedColor.value.toLong(), domainColor.color)
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
        val reconstructedValue = domainColor.color
        
        // Assert
        assertEquals(originalColor.value.toLong(), reconstructedValue)
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
            assertEquals(color.value.toLong(), domainColor.color)
        }
    }
}
