package ireader.presentation.ui.settings.recommendations

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
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

class SimilarTitlesSettingsViewModelTest {

    private class MockPreferenceStore : PreferenceStore {
        private val stringValues = mutableMapOf<String, String>()
        private val booleanValues = mutableMapOf<String, Boolean>()
        private val intValues = mutableMapOf<String, Int>()
        private val longValues = mutableMapOf<String, Long>()

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
            return object : Preference<T> {
                override fun key(): String = key
                override fun get(): T {
                    val raw = stringValues[key] ?: return defaultValue
                    return deserializer(raw)
                }
                override fun set(value: T) {
                    stringValues[key] = serializer(value)
                }
                override fun isSet(): Boolean = stringValues.containsKey(key)
                override fun delete() { stringValues.remove(key) }
                override fun defaultValue(): T = defaultValue
                override fun changes(): Flow<T> = MutableStateFlow(get())
                override fun stateIn(scope: CoroutineScope): StateFlow<T> = MutableStateFlow(get())
            }
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
    private val uiPreferences = UiPreferences(preferenceStore)
    private val viewModel = SimilarTitlesSettingsViewModel(uiPreferences)

    @Test
    fun `default preference values are correct`() {
        assertTrue(uiPreferences.showSimilarTitles().get())
        assertEquals(PreferenceValues.SimilarTitlesSource.AllSources, uiPreferences.similarTitlesSource().get())
        assertEquals(PreferenceValues.SimilarTitlesMatchMode.ByName, uiPreferences.similarTitlesMatchMode().get())
        assertEquals(10, uiPreferences.similarTitlesMaxCount().get())
    }

    @Test
    fun `setShowSimilarTitles updates preference`() {
        viewModel.setShowSimilarTitles(false)
        assertFalse(uiPreferences.showSimilarTitles().get())

        viewModel.setShowSimilarTitles(true)
        assertTrue(uiPreferences.showSimilarTitles().get())
    }

    @Test
    fun `setSimilarTitlesSource updates preference`() {
        viewModel.setSimilarTitlesSource(PreferenceValues.SimilarTitlesSource.AllSources)
        assertEquals(PreferenceValues.SimilarTitlesSource.AllSources, uiPreferences.similarTitlesSource().get())

        viewModel.setSimilarTitlesSource(PreferenceValues.SimilarTitlesSource.OtherSources)
        assertEquals(PreferenceValues.SimilarTitlesSource.OtherSources, uiPreferences.similarTitlesSource().get())
    }

    @Test
    fun `setSimilarTitlesMatchMode updates preference`() {
        viewModel.setSimilarTitlesMatchMode(PreferenceValues.SimilarTitlesMatchMode.ByGenre)
        assertEquals(PreferenceValues.SimilarTitlesMatchMode.ByGenre, uiPreferences.similarTitlesMatchMode().get())
    }

    @Test
    fun `setSimilarTitlesMaxCount updates preference`() {
        viewModel.setSimilarTitlesMaxCount(25)
        assertEquals(25, uiPreferences.similarTitlesMaxCount().get())

        viewModel.setSimilarTitlesMaxCount(0)
        assertEquals(0, uiPreferences.similarTitlesMaxCount().get())
    }

    @Test
    fun `dialog state changes work as expected`() {
        assertFalse(viewModel.showSourceDialog)
        viewModel.showSourceSelectionDialog()
        assertTrue(viewModel.showSourceDialog)
        viewModel.dismissSourceDialog()
        assertFalse(viewModel.showSourceDialog)

        assertFalse(viewModel.showMatchModeDialog)
        viewModel.showMatchModeSelectionDialog()
        assertTrue(viewModel.showMatchModeDialog)
        viewModel.dismissMatchModeDialog()
        assertFalse(viewModel.showMatchModeDialog)

        assertFalse(viewModel.showMaxCountDialog)
        viewModel.showMaxCountDialog()
        assertTrue(viewModel.showMaxCountDialog)
        viewModel.dismissMaxCountDialog()
        assertFalse(viewModel.showMaxCountDialog)
    }
}
