package ireader.data.remote

import io.github.jan.supabase.auth.auth
import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.prefs.SupabasePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MultiSupabaseClientProviderTest {

    private class TestPreferenceStore : PreferenceStore {
        val stringValues = mutableMapOf<String, String>()
        val booleanValues = mutableMapOf<String, Boolean>()
        val intValues = mutableMapOf<String, Int>()
        val longValues = mutableMapOf<String, Long>()
        val floatValues = mutableMapOf<String, Float>()

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
            return object : Preference<Float> {
                override fun key(): String = key
                override fun get(): Float = floatValues[key] ?: defaultValue
                override fun set(value: Float) { floatValues[key] = value }
                override fun isSet(): Boolean = floatValues.containsKey(key)
                override fun delete() { floatValues.remove(key) }
                override fun defaultValue(): Float = defaultValue
                override fun changes(): Flow<Float> = MutableStateFlow(get())
                override fun stateIn(scope: CoroutineScope): StateFlow<Float> = MutableStateFlow(get())
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

        override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> =
            throw UnsupportedOperationException()

        override fun <T> getObject(
            key: String,
            defaultValue: T,
            serializer: (T) -> String,
            deserializer: (String) -> T
        ): Preference<T> = throw UnsupportedOperationException()

        override fun <T> getJsonObject(
            key: String,
            defaultValue: T,
            serializer: KSerializer<T>,
            serializersModule: SerializersModule
        ): Preference<T> = throw UnsupportedOperationException()
    }

    @Test
    fun getAnalyticsConfig_returnsInitial_whenNoOverrides() {
        val store = TestPreferenceStore()
        val prefs = SupabasePreferences(store)
        val provider = MultiSupabaseClientProvider(
            preferences = prefs,
            initialAnalyticsUrl = "https://analytics.supabase.co",
            initialAnalyticsKey = "analytics-key"
        )

        val (url, key) = provider.getAnalyticsConfig()
        assertEquals("https://analytics.supabase.co", url)
        assertEquals("analytics-key", key)
    }

    @Test
    fun getAnalyticsConfig_returnsCustomCommunity_whenEnabled() {
        val store = TestPreferenceStore().apply {
            booleanValues[SupabasePreferences.USE_CUSTOM_COMMUNITY_SERVER] = true
            stringValues[SupabasePreferences.CUSTOM_COMMUNITY_URL] = "https://community.custom.io"
            stringValues[SupabasePreferences.CUSTOM_COMMUNITY_API_KEY] = "custom-key"
        }
        val prefs = SupabasePreferences(store)
        val provider = MultiSupabaseClientProvider(
            preferences = prefs,
            initialAnalyticsUrl = "https://analytics.supabase.co",
            initialAnalyticsKey = "analytics-key"
        )

        val (url, key) = provider.getAnalyticsConfig()
        assertEquals("https://community.custom.io", url)
        assertEquals("custom-key", key)
    }

    @Test
    fun getAnalyticsConfig_fallsBackToSingleProject_whenCustomSupabaseEnabled() {
        val store = TestPreferenceStore().apply {
            booleanValues[SupabasePreferences.USE_CUSTOM_SUPABASE] = true
            stringValues[SupabasePreferences.USER_SUPABASE_URL] = "https://single.supabase.co"
            stringValues[SupabasePreferences.USER_SUPABASE_ANON_KEY] = "single-key"
        }
        val prefs = SupabasePreferences(store)
        val provider = MultiSupabaseClientProvider(
            preferences = prefs,
            initialAnalyticsUrl = "https://analytics.supabase.co",
            initialAnalyticsKey = "analytics-key"
        )

        val (url, key) = provider.getAnalyticsConfig()
        assertEquals("https://single.supabase.co", url)
        assertEquals("single-key", key)
    }

    @Test
    fun getAnalyticsConfig_usesSpecificAnalyticsUrl_whenCustomSupabaseEnabled() {
        val store = TestPreferenceStore().apply {
            booleanValues[SupabasePreferences.USE_CUSTOM_SUPABASE] = true
            stringValues[SupabasePreferences.USER_SUPABASE_URL] = "https://single.supabase.co"
            stringValues[SupabasePreferences.USER_SUPABASE_ANON_KEY] = "single-key"
            stringValues[SupabasePreferences.ANALYTICS_URL] = "https://specific-analytics.supabase.co"
            stringValues[SupabasePreferences.ANALYTICS_API_KEY] = "specific-analytics-key"
        }
        val prefs = SupabasePreferences(store)
        val provider = MultiSupabaseClientProvider(
            preferences = prefs,
            initialAnalyticsUrl = "https://analytics.supabase.co",
            initialAnalyticsKey = "analytics-key"
        )

        val (url, key) = provider.getAnalyticsConfig()
        assertEquals("https://specific-analytics.supabase.co", url)
        assertEquals("specific-analytics-key", key)
    }

    @Test
    fun analyticsClient_hasAuthPluginInstalled() {
        val provider = MultiSupabaseClientProvider(
            initialAnalyticsUrl = "https://analytics.supabase.co",
            initialAnalyticsKey = "anon-key"
        )

        val client = provider.analyticsClient
        // client.auth must not throw IllegalStateException
        assertNotNull(client.auth)
    }
}
