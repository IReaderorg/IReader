package ireader.domain.services.tts_service.v2

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.prefs.ReaderPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for TTSPreferencesUseCase color methods.
 * Following TDD methodology: Write tests first, then implement.
 */
class TTSColorPreferencesUseCaseTest {
    
    private class MockPreferenceStore : PreferenceStore {
        private val longValues = mutableMapOf<String, Long>()
        
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
            throw UnsupportedOperationException("Not needed for these tests")
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
    private val ttsPreferencesUseCase = TTSPreferencesUseCase(readerPreferences, null)
    
    @Test
    fun `getCurrentParagraphColor should return default white color`() {
        // Arrange & Act
        val result = ttsPreferencesUseCase.getCurrentParagraphColor()
        
        // Assert
        assertEquals(0xFFFFFFFF, result, "Default current paragraph color should be white")
    }
    
    @Test
    fun `getCurrentParagraphHighlightColor should return default transparent color`() {
        // Arrange & Act
        val result = ttsPreferencesUseCase.getCurrentParagraphHighlightColor()
        
        // Assert
        assertEquals(0x00000000, result, "Default highlight color should be transparent")
    }
    
    @Test
    fun `getOtherTextColor should return default gray color`() {
        // Arrange & Act
        val result = ttsPreferencesUseCase.getOtherTextColor()
        
        // Assert
        assertEquals(0xFF808080, result, "Default other text color should be gray")
    }
    
    @Test
    fun `getCurrentParagraphColor should return custom color after setting`() {
        // Arrange
        val customColor = 0xFF00FF00L // Green
        readerPreferences.ttsCurrentParagraphColor().set(customColor)
        
        // Act
        val result = ttsPreferencesUseCase.getCurrentParagraphColor()
        
        // Assert
        assertEquals(customColor, result, "Should return custom current paragraph color")
    }
    
    @Test
    fun `getCurrentParagraphHighlightColor should return custom color after setting`() {
        // Arrange
        val customColor = 0x80FFFF00L // Semi-transparent yellow
        readerPreferences.ttsCurrentParagraphHighlightColor().set(customColor)
        
        // Act
        val result = ttsPreferencesUseCase.getCurrentParagraphHighlightColor()
        
        // Assert
        assertEquals(customColor, result, "Should return custom highlight color")
    }
    
    @Test
    fun `getOtherTextColor should return custom color after setting`() {
        // Arrange
        val customColor = 0xFF666666L // Darker gray
        readerPreferences.ttsOtherTextColor().set(customColor)
        
        // Act
        val result = ttsPreferencesUseCase.getOtherTextColor()
        
        // Assert
        assertEquals(customColor, result, "Should return custom other text color")
    }
    
    @Test
    fun `all color preferences should work independently`() {
        // Arrange
        val currentColor = 0xFFFFFFFF // White
        val highlightColor = 0x80FFFF00L // Semi-transparent yellow
        val otherColor = 0xFF808080 // Gray
        
        readerPreferences.ttsCurrentParagraphColor().set(currentColor)
        readerPreferences.ttsCurrentParagraphHighlightColor().set(highlightColor)
        readerPreferences.ttsOtherTextColor().set(otherColor)
        
        // Act
        val resultCurrent = ttsPreferencesUseCase.getCurrentParagraphColor()
        val resultHighlight = ttsPreferencesUseCase.getCurrentParagraphHighlightColor()
        val resultOther = ttsPreferencesUseCase.getOtherTextColor()
        
        // Assert
        assertEquals(currentColor, resultCurrent, "Current paragraph color should be white")
        assertEquals(highlightColor, resultHighlight, "Highlight color should be semi-transparent yellow")
        assertEquals(otherColor, resultOther, "Other text color should be gray")
    }
}
