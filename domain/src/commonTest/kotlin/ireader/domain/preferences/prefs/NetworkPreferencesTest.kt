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
 * Tests for NetworkPreferences.
 */
class NetworkPreferencesTest {

    private class MockPreferenceStore : PreferenceStore {
        private val stringValues = mutableMapOf<String, String>()
        private val booleanValues = mutableMapOf<String, Boolean>()
        private val intValues = mutableMapOf<String, Int>()

        override fun getString(key: String, defaultValue: String): Preference<String> {
            return object : Preference<String> {
                override fun key(): String = key
                override fun get(): String = stringValues[key] ?: defaultValue
                override fun set(value: String) { stringValues[key] = value }
                override fun isSet(): Boolean = stringValues.containsKey(key)
                override fun delete() { stringValues.remove(key) }
                override fun defaultValue(): String = defaultValue
                override fun changes(): Flow<String> = MutableStateFlow(get())
                override fun stateIn(scope: CoroutineScope): StateFlow<String> = MutableStateFlow(get())
            }
        }

        override fun getLong(key: String, defaultValue: Long): Preference<Long> {
            throw UnsupportedOperationException("Not needed for these tests")
        }

        override fun getInt(key: String, defaultValue: Int): Preference<Int> {
            return object : Preference<Int> {
                override fun key(): String = key
                override fun get(): Int = intValues[key] ?: defaultValue
                override fun set(value: Int) { intValues[key] = value }
                override fun isSet(): Boolean = intValues.containsKey(key)
                override fun delete() { intValues.remove(key) }
                override fun defaultValue(): Int = defaultValue
                override fun changes(): Flow<Int> = MutableStateFlow(get())
                override fun stateIn(scope: CoroutineScope): StateFlow<Int> = MutableStateFlow(get())
            }
        }

        override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
            throw UnsupportedOperationException("Not needed for these tests")
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
    }

    private val preferenceStore = MockPreferenceStore()
    private val networkPreferences = NetworkPreferences(preferenceStore)

    @Test
    fun `default user agent should be set`() {
        val ua = networkPreferences.customUserAgent().get()
        assertEquals(NetworkPreferences.DEFAULT_USER_AGENT, ua)
    }

    @Test
    fun `use default user agent should be true by default`() {
        assertTrue(networkPreferences.useDefaultUserAgent().get())
    }

    @Test
    fun `custom user agent can be set`() {
        val customUA = "CustomAgent/1.0"
        networkPreferences.customUserAgent().set(customUA)
        assertEquals(customUA, networkPreferences.customUserAgent().get())
    }

    @Test
    fun `proxy is disabled by default`() {
        assertFalse(networkPreferences.proxyEnabled().get())
    }

    @Test
    fun `proxy can be enabled`() {
        networkPreferences.proxyEnabled().set(true)
        assertTrue(networkPreferences.proxyEnabled().get())
    }

    @Test
    fun `proxy host can be set`() {
        networkPreferences.proxyHost().set("proxy.example.com")
        assertEquals("proxy.example.com", networkPreferences.proxyHost().get())
    }

    @Test
    fun `proxy port can be set`() {
        networkPreferences.proxyPort().set(8080)
        assertEquals(8080, networkPreferences.proxyPort().get())
    }

    @Test
    fun `default proxy host is empty`() {
        assertEquals("", networkPreferences.proxyHost().get())
    }

    @Test
    fun `default proxy port is zero`() {
        assertEquals(0, networkPreferences.proxyPort().get())
    }
}