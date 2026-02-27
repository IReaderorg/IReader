package ireader.domain.usecases.translate

import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.prefs.TranslationPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for translation engine initialization when auto-translate is enabled
 * Following TDD: RED → GREEN → REFACTOR
 */
class TranslationEngineInitializationTest {
    
    @Test
    fun `should check if engine requires initialization when auto-translate is enabled`() {
        // Arrange
        val mockPreferenceStore = MockPreferenceStore()
        val translationPreferences = TranslationPreferences(mockPreferenceStore)
        val readerPreferences = ReaderPreferences(mockPreferenceStore)
        val translationEnginesManager = TranslationEnginesManager(
            readerPreferences = readerPreferences,
            httpClients = MockHttpClients(),
            pluginManager = null
        )
        
        // Set Google ML as the default engine (id = 0)
        readerPreferences.translatorEngine().set(0L)
        
        // Act
        val engine = translationEnginesManager.get()
        val requiresInit = engine.requiresInitialization
        
        // Assert
        // Google ML requires initialization on Android
        assertTrue(requiresInit, "Google ML should require initialization")
    }
    
    @Test
    fun `should return false for engines that don't require initialization`() {
        // Arrange
        val mockPreferenceStore = MockPreferenceStore()
        val readerPreferences = ReaderPreferences(mockPreferenceStore)
        val translationEnginesManager = TranslationEnginesManager(
            readerPreferences = readerPreferences,
            httpClients = MockHttpClients(),
            pluginManager = null
        )
        
        // Set Gemini as the engine (id = 8) - doesn't require initialization
        readerPreferences.translatorEngine().set(8L)
        
        // Act
        val engine = translationEnginesManager.get()
        val requiresInit = engine.requiresInitialization
        
        // Assert
        assertFalse(requiresInit, "Gemini should not require initialization")
    }
}

// Mock classes for testing
class MockPreferenceStore : PreferenceStore {
    private val prefs = mutableMapOf<String, Any>()
    
    override fun getBoolean(key: String, defaultValue: Boolean) = 
        MockBooleanPreference(key, defaultValue, prefs)
    
    override fun getLong(key: String, defaultValue: Long) = 
        MockLongPreference(key, defaultValue, prefs)
    
    override fun getInt(key: String, defaultValue: Int) = 
        MockIntPreference(key, defaultValue, prefs)
    
    override fun getString(key: String, defaultValue: String) = 
        MockStringPreference(key, defaultValue, prefs)
    
    override fun <T : Enum<T>> getEnum(key: String, defaultValue: T, clazz: Class<T>) = 
        MockEnumPreference(key, defaultValue, prefs, clazz)
}

class MockBooleanPreference(
    private val key: String,
    private val defaultValue: Boolean,
    private val prefs: MutableMap<String, Any>
) : ireader.core.prefs.Preference<Boolean> {
    override fun get(): Boolean = prefs[key] as? Boolean ?: defaultValue
    override fun set(value: Boolean) { prefs[key] = value }
    override fun isSet(): Boolean = prefs.containsKey(key)
    override fun delete() { prefs.remove(key) }
    override fun defaultValue(): Boolean = defaultValue
    override fun key(): String = key
    override fun changes() = kotlinx.coroutines.flow.MutableStateFlow(get())
    override fun stateIn(scope: kotlinx.coroutines.CoroutineScope) = changes()
}

class MockLongPreference(
    private val key: String,
    private val defaultValue: Long,
    private val prefs: MutableMap<String, Any>
) : ireader.core.prefs.Preference<Long> {
    override fun get(): Long = prefs[key] as? Long ?: defaultValue
    override fun set(value: Long) { prefs[key] = value }
    override fun isSet(): Boolean = prefs.containsKey(key)
    override fun delete() { prefs.remove(key) }
    override fun defaultValue(): Long = defaultValue
    override fun key(): String = key
    override fun changes() = kotlinx.coroutines.flow.MutableStateFlow(get())
    override fun stateIn(scope: kotlinx.coroutines.CoroutineScope) = changes()
}

class MockIntPreference(
    private val key: String,
    private val defaultValue: Int,
    private val prefs: MutableMap<String, Any>
) : ireader.core.prefs.Preference<Int> {
    override fun get(): Int = prefs[key] as? Int ?: defaultValue
    override fun set(value: Int) { prefs[key] = value }
    override fun isSet(): Boolean = prefs.containsKey(key)
    override fun delete() { prefs.remove(key) }
    override fun defaultValue(): Int = defaultValue
    override fun key(): String = key
    override fun changes() = kotlinx.coroutines.flow.MutableStateFlow(get())
    override fun stateIn(scope: kotlinx.coroutines.CoroutineScope) = changes()
}

class MockStringPreference(
    private val key: String,
    private val defaultValue: String,
    private val prefs: MutableMap<String, Any>
) : ireader.core.prefs.Preference<String> {
    override fun get(): String = prefs[key] as? String ?: defaultValue
    override fun set(value: String) { prefs[key] = value }
    override fun isSet(): Boolean = prefs.containsKey(key)
    override fun delete() { prefs.remove(key) }
    override fun defaultValue(): String = defaultValue
    override fun key(): String = key
    override fun changes() = kotlinx.coroutines.flow.MutableStateFlow(get())
    override fun stateIn(scope: kotlinx.coroutines.CoroutineScope) = changes()
}

class MockEnumPreference<T : Enum<T>>(
    private val key: String,
    private val defaultValue: T,
    private val prefs: MutableMap<String, Any>,
    private val clazz: Class<T>
) : ireader.core.prefs.Preference<T> {
    override fun get(): T = prefs[key] as? T ?: defaultValue
    override fun set(value: T) { prefs[key] = value }
    override fun isSet(): Boolean = prefs.containsKey(key)
    override fun delete() { prefs.remove(key) }
    override fun defaultValue(): T = defaultValue
    override fun key(): String = key
    override fun changes() = kotlinx.coroutines.flow.MutableStateFlow(get())
    override fun stateIn(scope: kotlinx.coroutines.CoroutineScope) = changes()
}

class MockHttpClients : ireader.core.http.HttpClients {
    override val default: ireader.core.http.HttpClient
        get() = throw NotImplementedError("Mock HttpClient not needed for this test")
    override val cloudflare: ireader.core.http.HttpClient
        get() = throw NotImplementedError("Mock HttpClient not needed for this test")
}
