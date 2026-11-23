package ireader.domain.preferences

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.prefs.ReadingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals

class ReaderPreferencesReadingModeTest {

    @Test
    fun `setActiveReadingMode updates the preference the reader observes`() {
        val store = FakePreferenceStore()
        val preferences = ReaderPreferences(store)

        preferences.setActiveReadingMode(ReadingMode.Page)

        assertEquals(ReadingMode.Page, preferences.readingMode().get())
        assertEquals(ReadingMode.Page, preferences.defaultReadingMode().get())
    }

    private class FakePreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, Any>()

        override fun getString(key: String, defaultValue: String) = pref(key, defaultValue)
        override fun getLong(key: String, defaultValue: Long) = pref(key, defaultValue)
        override fun getInt(key: String, defaultValue: Int) = pref(key, defaultValue)
        override fun getFloat(key: String, defaultValue: Float) = pref(key, defaultValue)
        override fun getBoolean(key: String, defaultValue: Boolean) = pref(key, defaultValue)
        override fun getStringSet(key: String, defaultValue: Set<String>) = pref(key, defaultValue)

        override fun <T> getObject(
            key: String,
            defaultValue: T,
            serializer: (T) -> String,
            deserializer: (String) -> T,
        ) = object : Preference<T> {
            override fun key(): String = key
            override fun get(): T = (values[key] as? String)?.let(deserializer) ?: defaultValue
            override fun set(value: T) { values[key] = serializer(value) }
            override fun isSet(): Boolean = key in values
            override fun delete() { values.remove(key) }
            override fun defaultValue(): T = defaultValue
            override fun changes(): Flow<T> = MutableStateFlow(get())
            override fun stateIn(scope: CoroutineScope): StateFlow<T> = MutableStateFlow(get())
        }

        override fun <T> getJsonObject(
            key: String,
            defaultValue: T,
            serializer: KSerializer<T>,
            serializersModule: SerializersModule,
        ): Preference<T> = throw UnsupportedOperationException()

        private inline fun <reified T> pref(key: String, defaultValue: T): Preference<T> =
            object : Preference<T> {
                override fun key(): String = key
                override fun get(): T = values[key] as? T ?: defaultValue
                override fun set(value: T) { values[key] = value as Any }
                override fun isSet(): Boolean = key in values
                override fun delete() { values.remove(key) }
                override fun defaultValue(): T = defaultValue
                override fun changes(): Flow<T> = MutableStateFlow(get())
                override fun stateIn(scope: CoroutineScope): StateFlow<T> = MutableStateFlow(get())
            }
    }
}
