package ireader.domain.usecases.translate

import ireader.core.http.BrowserEngine
import ireader.core.http.CloudflareBypassHandler
import ireader.core.http.CookieSynchronizer
import ireader.core.http.HttpClientsInterface
import ireader.core.http.NetworkConfig
import ireader.core.http.NoOpCloudflareBypassHandler
import ireader.core.http.SSLConfiguration
import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.preferences.prefs.ReaderPreferences
import io.ktor.client.HttpClient
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for translation engine initialization when auto-translate is enabled
 * Following TDD: RED → GREEN → REFACTOR
 */
class TranslationEngineInitializationTest {

    @Test
    fun `should return true for engines that require initialization`() {
        // Arrange - Create a test engine that requires initialization
        val engine = TestEngine(requiresInit = true)

        // Act
        val requiresInit = engine.requiresInitialization

        // Assert
        assertTrue(requiresInit, "Engine should require initialization when configured")
    }

    @Test
    fun `should return false for engines that don't require initialization`() {
        // Arrange - Create a test engine that doesn't require initialization
        val engine = TestEngine(requiresInit = false)

        // Act
        val requiresInit = engine.requiresInitialization

        // Assert
        assertFalse(requiresInit, "Engine should not require initialization when configured")
    }

    @Test
    fun `should verify engine id and name are accessible`() {
        // Arrange
        val engine = TestEngine(requiresInit = false)

        // Act & Assert
        assertTrue(engine.id >= 0, "Engine should have a valid ID")
        assertTrue(engine.engineName.isNotEmpty(), "Engine should have a name")
    }
}

/**
 * Test implementation of TranslateEngine for testing purposes.
 */
private class TestEngine(
    override val requiresInitialization: Boolean
) : TranslateEngine() {
    override val id: Long = 999L
    override val engineName: String = "Test Engine"

    override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (ireader.i18n.UiText) -> Unit
    ) {
        onSuccess(texts)
    }
}

// Mock classes for testing
class MockHttpClients : HttpClientsInterface {
    override val browser: BrowserEngine
        get() = throw NotImplementedError("Mock BrowserEngine not needed for this test")
    override val default: HttpClient
        get() = throw NotImplementedError("Mock HttpClient not needed for this test")
    override val cloudflareClient: HttpClient
        get() = throw NotImplementedError("Mock HttpClient not needed for this test")
    override val config: NetworkConfig
        get() = throw NotImplementedError("Mock NetworkConfig not needed for this test")
    override val sslConfig: SSLConfiguration
        get() = throw NotImplementedError("Mock SSLConfiguration not needed for this test")
    override val cookieSynchronizer: CookieSynchronizer
        get() = throw NotImplementedError("Mock CookieSynchronizer not needed for this test")
    override val cloudflareBypassHandler: CloudflareBypassHandler
        get() = NoOpCloudflareBypassHandler
}

class MockReaderPreferences : ReaderPreferences(MockPreferenceStore())

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
