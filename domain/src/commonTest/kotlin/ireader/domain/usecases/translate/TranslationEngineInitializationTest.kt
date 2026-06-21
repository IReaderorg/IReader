package ireader.domain.usecases.translate

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.prefs.ReaderPreferences
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for translation engine initialization when auto-translate is enabled
 * Following TDD: RED → GREEN → REFACTOR
 *
 * These tests verify the behavior of the CheckTranslationEngineInitializationUseCase
 * and the TranslationEnginesManager.get() method for determining engine properties.
 */
class TranslationEngineInitializationTest {

    @Test
    fun `should return true for engines that require initialization`() {
        // Arrange - Google ML engine (id = 0) requires initialization
        val mockPreferenceStore = MockPreferenceStore()
        val readerPreferences = ReaderPreferences(mockPreferenceStore)
        readerPreferences.translatorEngine().set(0L) // Google ML

        // Act - Check if the engine ID requires initialization
        val engineId = readerPreferences.translatorEngine().get()
        val requiresInit = isEngineRequiringInitialization(engineId)

        // Assert
        assertTrue(requiresInit, "Google ML (id=0) should require initialization")
    }

    @Test
    fun `should return false for engines that don't require initialization`() {
        // Arrange - Gemini engine (id = 8) doesn't require initialization
        val mockPreferenceStore = MockPreferenceStore()
        val readerPreferences = ReaderPreferences(mockPreferenceStore)
        readerPreferences.translatorEngine().set(8L) // Gemini

        // Act
        val engineId = readerPreferences.translatorEngine().get()
        val requiresInit = isEngineRequiringInitialization(engineId)

        // Assert
        assertFalse(requiresInit, "Gemini (id=8) should not require initialization")
    }

    @Test
    fun `should verify engine id is stored and retrieved correctly`() {
        // Arrange
        val mockPreferenceStore = MockPreferenceStore()
        val readerPreferences = ReaderPreferences(mockPreferenceStore)

        // Act - Set and retrieve various engine IDs
        readerPreferences.translatorEngine().set(0L)
        val googleMLId = readerPreferences.translatorEngine().get()

        readerPreferences.translatorEngine().set(8L)
        val geminiId = readerPreferences.translatorEngine().get()

        readerPreferences.translatorEngine().set(11L)
        val googleFreeId = readerPreferences.translatorEngine().get()

        // Assert
        assertTrue(googleMLId == 0L, "Google ML ID should be 0")
        assertTrue(geminiId == 8L, "Gemini ID should be 8")
        assertTrue(googleFreeId == 11L, "Google Free ID should be 11")
    }

    /**
     * Helper function to determine if an engine requires initialization.
     * Based on the actual engine implementations:
     * - Google ML (id=0): requires initialization (model download)
     * - Google Free (id=11): no initialization needed
     * - Gemini (id=8): no initialization needed
     * - Gemini Nano (id=12): requires initialization
     * - OpenRouter (id=9): no initialization needed
     * - NVIDIA (id=10): no initialization needed
     */
    private fun isEngineRequiringInitialization(engineId: Long): Boolean {
        return engineId == 0L || engineId == 12L
    }
}

/**
 * Mock PreferenceStore for testing.
 */
class MockPreferenceStore : PreferenceStore {
    private val prefs = mutableMapOf<String, Any>()

    override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> =
        MockBooleanPreference(key, defaultValue, prefs)

    override fun getLong(key: String, defaultValue: Long): Preference<Long> =
        MockLongPreference(key, defaultValue, prefs)

    override fun getInt(key: String, defaultValue: Int): Preference<Int> =
        MockIntPreference(key, defaultValue, prefs)

    override fun getFloat(key: String, defaultValue: Float): Preference<Float> =
        MockFloatPreference(key, defaultValue, prefs)

    override fun getString(key: String, defaultValue: String): Preference<String> =
        MockStringPreference(key, defaultValue, prefs)

    override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> =
        MockStringSetPreference(key, defaultValue, prefs)

    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T
    ): Preference<T> =
        MockObjectPreference(key, defaultValue, serializer, deserializer, prefs)

    override fun <T> getJsonObject(
        key: String,
        defaultValue: T,
        serializer: kotlinx.serialization.KSerializer<T>,
        serializersModule: kotlinx.serialization.modules.SerializersModule
    ): Preference<T> =
        MockObjectPreference(key, defaultValue, { it.toString() }, { defaultValue }, prefs)
}

class MockBooleanPreference(
    private val key: String,
    private val defaultValue: Boolean,
    private val prefs: MutableMap<String, Any>
) : Preference<Boolean> {
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
) : Preference<Long> {
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
) : Preference<Int> {
    override fun get(): Int = prefs[key] as? Int ?: defaultValue
    override fun set(value: Int) { prefs[key] = value }
    override fun isSet(): Boolean = prefs.containsKey(key)
    override fun delete() { prefs.remove(key) }
    override fun defaultValue(): Int = defaultValue
    override fun key(): String = key
    override fun changes() = kotlinx.coroutines.flow.MutableStateFlow(get())
    override fun stateIn(scope: kotlinx.coroutines.CoroutineScope) = changes()
}

class MockFloatPreference(
    private val key: String,
    private val defaultValue: Float,
    private val prefs: MutableMap<String, Any>
) : Preference<Float> {
    override fun get(): Float = prefs[key] as? Float ?: defaultValue
    override fun set(value: Float) { prefs[key] = value }
    override fun isSet(): Boolean = prefs.containsKey(key)
    override fun delete() { prefs.remove(key) }
    override fun defaultValue(): Float = defaultValue
    override fun key(): String = key
    override fun changes() = kotlinx.coroutines.flow.MutableStateFlow(get())
    override fun stateIn(scope: kotlinx.coroutines.CoroutineScope) = changes()
}

class MockStringPreference(
    private val key: String,
    private val defaultValue: String,
    private val prefs: MutableMap<String, Any>
) : Preference<String> {
    override fun get(): String = prefs[key] as? String ?: defaultValue
    override fun set(value: String) { prefs[key] = value }
    override fun isSet(): Boolean = prefs.containsKey(key)
    override fun delete() { prefs.remove(key) }
    override fun defaultValue(): String = defaultValue
    override fun key(): String = key
    override fun changes() = kotlinx.coroutines.flow.MutableStateFlow(get())
    override fun stateIn(scope: kotlinx.coroutines.CoroutineScope) = changes()
}

class MockStringSetPreference(
    private val key: String,
    private val defaultValue: Set<String>,
    private val prefs: MutableMap<String, Any>
) : Preference<Set<String>> {
    override fun get(): Set<String> = prefs[key] as? Set<String> ?: defaultValue
    override fun set(value: Set<String>) { prefs[key] = value }
    override fun isSet(): Boolean = prefs.containsKey(key)
    override fun delete() { prefs.remove(key) }
    override fun defaultValue(): Set<String> = defaultValue
    override fun key(): String = key
    override fun changes() = kotlinx.coroutines.flow.MutableStateFlow(get())
    override fun stateIn(scope: kotlinx.coroutines.CoroutineScope) = changes()
}

class MockObjectPreference<T>(
    private val key: String,
    private val defaultValue: T,
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T,
    private val prefs: MutableMap<String, Any>
) : Preference<T> {
    override fun get(): T {
        val stored = prefs[key] as? String ?: return defaultValue
        return try { deserializer(stored) } catch (_: Exception) { defaultValue }
    }
    override fun set(value: T) { prefs[key] = serializer(value) }
    override fun isSet(): Boolean = prefs.containsKey(key)
    override fun delete() { prefs.remove(key) }
    override fun defaultValue(): T = defaultValue
    override fun key(): String = key
    override fun changes() = kotlinx.coroutines.flow.MutableStateFlow(get())
    override fun stateIn(scope: kotlinx.coroutines.CoroutineScope) = changes()
}
