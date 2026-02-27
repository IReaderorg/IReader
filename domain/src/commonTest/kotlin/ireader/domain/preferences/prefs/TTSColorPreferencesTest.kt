package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for TTS Color Customization Preferences.
 * Following TDD methodology: Write tests first, then implement.
 * 
 * Requirements:
 * - Current reading paragraph color
 * - Current paragraph highlight color (can be none/transparent)
 * - Other text color (non-current paragraphs)
 * - Background color (already exists, but testing for completeness)
 */
class TTSColorPreferencesTest {
    
    private class MockPreferenceStore : PreferenceStore {
        private val longValues = mutableMapOf<String, Long>()
        private val booleanValues = mutableMapOf<String, Boolean>()
        
        override fun getLong(key: String, defaultValue: Long): Preference<Long> {
            return object : Preference<Long> {
                override fun key(): String = key
                override fun get(): Long = longValues[key] ?: defaultValue
                override fun set(value: Long) { longValues[key] = value }
                override fun isSet(): Boolean = longValues.containsKey(key)
                override fun delete() { longValues.remove(key) }
                override fun defaultValue(): Long = defaultValue
                override fun changes(): Flow<Long> = MutableStateFlow(get())
                override fun stateIn(scope: CoroutineScope): StateFlow<Long> = MutableStateFlow(get())
            }
        }
        
        override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
            return object : Preference<Boolean> {
                override fun key(): String = key
                override fun get(): Boolean = booleanValues[key] ?: defaultValue
                override fun set(value: Boolean) { booleanValues[key] = value }
                override fun isSet(): Boolean = booleanValues.containsKey(key)
                override fun delete() { booleanValues.remove(key) }
                override fun defaultValue(): Boolean = defaultValue
                override fun changes(): Flow<Boolean> = MutableStateFlow(get())
                override fun stateIn(scope: CoroutineScope): StateFlow<Boolean> = MutableStateFlow(get())
            }
        }
        
        override fun getString(key: String, defaultValue: String): Preference<String> {
            throw UnsupportedOperationException("Not needed for these tests")
        }
        
        override fun getInt(key: String, defaultValue: Int): Preference<Int> {
            throw UnsupportedOperationException("Not needed for these tests")
        }
        
        override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
            throw UnsupportedOperationException("Not needed for these tests")
        }
        
        override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
            throw UnsupportedOperationException("Not needed for these tests")
        }
        
        override fun <T> getObject(
            key: String,
            defaultValue: T,
            serializer: (T) -> String,
            deserializer: (String) -> T
        ): Preference<T> {
            throw UnsupportedOperationException("Not needed for these tests")
        }
        
        override fun <T> getJsonObject(
            key: String,
            defaultValue: T,
            serializer: KSerializer<T>,
            serializersModule: SerializersModule
        ): Preference<T> {
            throw UnsupportedOperationException("Not needed for these tests")
        }
        
        override fun <T : Enum<T>> getEnum(
            key: String,
            defaultValue: T,
            serializer: (T) -> String,
            deserializer: (String) -> T
        ): Preference<T> {
            throw UnsupportedOperationException("Not needed for these tests")
        }
    }
    
    private val preferenceStore = MockPreferenceStore()
    private val readerPreferences = ReaderPreferences(preferenceStore)
    
    // ==================== Current Reading Paragraph Color Tests ====================
    
    @Test
    fun `ttsCurrentParagraphColor should default to white`() {
        // Arrange & Act
        val result = readerPreferences.ttsCurrentParagraphColor().get()
        
        // Assert
        assertEquals(0xFFFFFFFF, result, "Default current paragraph color should be white (0xFFFFFFFF)")
    }
    
    @Test
    fun `ttsCurrentParagraphColor should store custom color`() {
        // Arrange
        val preference = readerPreferences.ttsCurrentParagraphColor()
        val customColor = 0xFF00FF00L // Green
        
        // Act
        preference.set(customColor)
        
        // Assert
        assertEquals(customColor, preference.get(), "Should store custom current paragraph color")
    }
    
    @Test
    fun `ttsCurrentParagraphColor should allow changing color multiple times`() {
        // Arrange
        val preference = readerPreferences.ttsCurrentParagraphColor()
        val color1 = 0xFFFF0000L // Red
        val color2 = 0xFF0000FFL // Blue
        
        // Act
        preference.set(color1)
        val result1 = preference.get()
        preference.set(color2)
        val result2 = preference.get()
        
        // Assert
        assertEquals(color1, result1, "Should store first color")
        assertEquals(color2, result2, "Should update to second color")
    }
    
    // ==================== Current Paragraph Highlight Color Tests ====================
    
    @Test
    fun `ttsCurrentParagraphHighlightColor should default to transparent`() {
        // Arrange & Act
        val result = readerPreferences.ttsCurrentParagraphHighlightColor().get()
        
        // Assert
        assertEquals(0x00000000, result, "Default highlight color should be transparent (0x00000000)")
    }
    
    @Test
    fun `ttsCurrentParagraphHighlightColor should store custom highlight color`() {
        // Arrange
        val preference = readerPreferences.ttsCurrentParagraphHighlightColor()
        val highlightColor = 0x80FFFF00L // Semi-transparent yellow
        
        // Act
        preference.set(highlightColor)
        
        // Assert
        assertEquals(highlightColor, preference.get(), "Should store custom highlight color")
    }
    
    @Test
    fun `ttsCurrentParagraphHighlightColor should allow setting to transparent to disable`() {
        // Arrange
        val preference = readerPreferences.ttsCurrentParagraphHighlightColor()
        preference.set(0x80FFFF00L) // Set to yellow first
        
        // Act
        preference.set(0x00000000L) // Set to transparent to disable
        
        // Assert
        assertEquals(0x00000000L, preference.get(), "Should allow disabling highlight by setting transparent")
    }
    
    // ==================== Other Text Color Tests ====================
    
    @Test
    fun `ttsOtherTextColor should default to gray`() {
        // Arrange & Act
        val result = readerPreferences.ttsOtherTextColor().get()
        
        // Assert
        assertEquals(0xFF808080, result, "Default other text color should be gray (0xFF808080)")
    }
    
    @Test
    fun `ttsOtherTextColor should store custom color`() {
        // Arrange
        val preference = readerPreferences.ttsOtherTextColor()
        val customColor = 0xFF666666L // Darker gray
        
        // Act
        preference.set(customColor)
        
        // Assert
        assertEquals(customColor, preference.get(), "Should store custom other text color")
    }
    
    @Test
    fun `ttsOtherTextColor should allow different color from current paragraph`() {
        // Arrange
        val currentParagraphPref = readerPreferences.ttsCurrentParagraphColor()
        val otherTextPref = readerPreferences.ttsOtherTextColor()
        val currentColor = 0xFFFFFFFF // White
        val otherColor = 0xFF808080 // Gray
        
        // Act
        currentParagraphPref.set(currentColor)
        otherTextPref.set(otherColor)
        
        // Assert
        assertEquals(currentColor, currentParagraphPref.get(), "Current paragraph should be white")
        assertEquals(otherColor, otherTextPref.get(), "Other text should be gray")
        assertTrue(currentParagraphPref.get() != otherTextPref.get(), "Colors should be different")
    }
    
    // ==================== Background Color Tests (existing feature) ====================
    
    @Test
    fun `ttsBackgroundColor should default to dark color`() {
        // Arrange & Act
        val result = readerPreferences.ttsBackgroundColor().get()
        
        // Assert
        assertEquals(0xFF1E1E1E, result, "Default background color should be dark (0xFF1E1E1E)")
    }
    
    @Test
    fun `ttsBackgroundColor should store custom background color`() {
        // Arrange
        val preference = readerPreferences.ttsBackgroundColor()
        val customColor = 0xFF000000L // Black
        
        // Act
        preference.set(customColor)
        
        // Assert
        assertEquals(customColor, preference.get(), "Should store custom background color")
    }
    
    // ==================== Use Custom Colors Flag Tests ====================
    
    @Test
    fun `ttsUseCustomColors should default to false`() {
        // Arrange & Act
        val result = readerPreferences.ttsUseCustomColors().get()
        
        // Assert
        assertFalse(result, "Custom colors should be disabled by default")
    }
    
    @Test
    fun `ttsUseCustomColors should enable custom colors`() {
        // Arrange
        val preference = readerPreferences.ttsUseCustomColors()
        
        // Act
        preference.set(true)
        
        // Assert
        assertTrue(preference.get(), "Should enable custom colors")
    }
    
    @Test
    fun `ttsUseCustomColors should toggle between enabled and disabled`() {
        // Arrange
        val preference = readerPreferences.ttsUseCustomColors()
        
        // Act
        preference.set(true)
        val enabled = preference.get()
        preference.set(false)
        val disabled = preference.get()
        
        // Assert
        assertTrue(enabled, "Should be enabled after setting true")
        assertFalse(disabled, "Should be disabled after setting false")
    }
    
    // ==================== Integration Tests ====================
    
    @Test
    fun `all TTS color preferences should work together`() {
        // Arrange
        val useCustom = readerPreferences.ttsUseCustomColors()
        val background = readerPreferences.ttsBackgroundColor()
        val currentParagraph = readerPreferences.ttsCurrentParagraphColor()
        val highlight = readerPreferences.ttsCurrentParagraphHighlightColor()
        val otherText = readerPreferences.ttsOtherTextColor()
        
        // Act
        useCustom.set(true)
        background.set(0xFF000000L) // Black background
        currentParagraph.set(0xFFFFFFFF) // White current paragraph
        highlight.set(0x80FFFF00L) // Semi-transparent yellow highlight
        otherText.set(0xFF808080) // Gray other text
        
        // Assert
        assertTrue(useCustom.get(), "Custom colors should be enabled")
        assertEquals(0xFF000000L, background.get(), "Background should be black")
        assertEquals(0xFFFFFFFF, currentParagraph.get(), "Current paragraph should be white")
        assertEquals(0x80FFFF00L, highlight.get(), "Highlight should be semi-transparent yellow")
        assertEquals(0xFF808080, otherText.get(), "Other text should be gray")
    }
    
    @Test
    fun `color preferences should persist independently`() {
        // Arrange
        val currentParagraph = readerPreferences.ttsCurrentParagraphColor()
        val otherText = readerPreferences.ttsOtherTextColor()
        
        // Act
        currentParagraph.set(0xFFFF0000L) // Red
        
        // Assert
        assertEquals(0xFFFF0000L, currentParagraph.get(), "Current paragraph color should be red")
        assertEquals(0xFF808080, otherText.get(), "Other text color should remain at default gray")
    }
}
