package ireader.presentation.ui.settings.translation_suite

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.prefs.TranslationPreferences
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

class TranslationSuiteViewModelTest {

    private class MockPreferenceStore : PreferenceStore {
        private val stringValues = mutableMapOf<String, String>()
        private val booleanValues = mutableMapOf<String, Boolean>()
        private val intValues = mutableMapOf<String, Int>()
        private val longValues = mutableMapOf<String, Long>()
        private val floatValues = mutableMapOf<String, Float>()

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

        override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> = throw UnsupportedOperationException()
        override fun <T> getObject(key: String, defaultValue: T, serializer: (T) -> String, deserializer: (String) -> T): Preference<T> = throw UnsupportedOperationException()
        override fun <T> getJsonObject(key: String, defaultValue: T, serializer: KSerializer<T>, serializersModule: SerializersModule): Preference<T> = throw UnsupportedOperationException()
    }

    @Test
    fun testEngineAndLanguageSwitching() {
        val prefStore = MockPreferenceStore()
        val readerPrefs = ReaderPreferences(prefStore)
        val translationPrefs = TranslationPreferences(prefStore)

        val viewModel = TranslationSuiteViewModel(readerPrefs, translationPrefs)

        viewModel.setEngineId(8L)
        assertEquals(8L, viewModel.state.value.selectedEngineId)

        viewModel.setTargetLanguage("Spanish")
        assertEquals("Spanish", viewModel.state.value.targetLanguage)

        viewModel.setOpenAIApiKey("test-openai-key")
        assertEquals("test-openai-key", viewModel.state.value.openAIApiKey)
        assertEquals("test-openai-key", readerPrefs.openAIApiKey().get())

        viewModel.setGeminiApiKey("test-gemini-key")
        assertEquals("test-gemini-key", viewModel.state.value.geminiApiKey)
        assertEquals("test-gemini-key", readerPrefs.geminiApiKey().get())

        viewModel.setDeepSeekApiKey("test-deepseek-key")
        assertEquals("test-deepseek-key", viewModel.state.value.deepSeekApiKey)
        assertEquals("test-deepseek-key", readerPrefs.deepSeekApiKey().get())

        viewModel.setClaudeApiKey("test-claude-key")
        assertEquals("test-claude-key", viewModel.state.value.claudeApiKey)
        assertEquals("test-claude-key", readerPrefs.claudeApiKey().get())

        viewModel.setClaudeModel("claude-3-opus-20240229")
        assertEquals("claude-3-opus-20240229", viewModel.state.value.claudeModel)
        assertEquals("claude-3-opus-20240229", readerPrefs.claudeModel().get())
    }

    @Test
    fun testContextAndStyleSettings() {
        val prefStore = MockPreferenceStore()
        val readerPrefs = ReaderPreferences(prefStore)
        val translationPrefs = TranslationPreferences(prefStore)

        val viewModel = TranslationSuiteViewModel(readerPrefs, translationPrefs)

        viewModel.setContentType(ireader.domain.data.engines.ContentType.LITERARY)
        assertEquals(ireader.domain.data.engines.ContentType.LITERARY, viewModel.state.value.contentType)

        viewModel.setToneType(ireader.domain.data.engines.ToneType.FORMAL)
        assertEquals(ireader.domain.data.engines.ToneType.FORMAL, viewModel.state.value.toneType)

        viewModel.setPreserveStyle(true)
        assertTrue(viewModel.state.value.preserveStyle)

        viewModel.setCustomPrompt("Translate in elegant wuxia prose")
        assertEquals("Translate in elegant wuxia prose", viewModel.state.value.customPrompt)
    }


    @Test
    fun testGlossaryOperations() {
        val prefStore = MockPreferenceStore()
        val readerPrefs = ReaderPreferences(prefStore)
        val translationPrefs = TranslationPreferences(prefStore)

        val viewModel = TranslationSuiteViewModel(readerPrefs, translationPrefs)

        assertTrue(viewModel.state.value.glossaryTerms.isEmpty())

        viewModel.addGlossaryTerm("Xiao Yan", "Sean", "Protagonist")
        assertEquals(1, viewModel.state.value.glossaryTerms.size)
        assertEquals("Xiao Yan", viewModel.state.value.glossaryTerms.first().sourceTerm)
        assertEquals("Sean", viewModel.state.value.glossaryTerms.first().targetTerm)

        val termId = viewModel.state.value.glossaryTerms.first().id
        viewModel.deleteGlossaryTerm(termId)
        assertTrue(viewModel.state.value.glossaryTerms.isEmpty())
    }

    @Test
    fun testPresetToggles() {
        val prefStore = MockPreferenceStore()
        val readerPrefs = ReaderPreferences(prefStore)
        val translationPrefs = TranslationPreferences(prefStore)

        val viewModel = TranslationSuiteViewModel(readerPrefs, translationPrefs)

        val initialNavPreset = viewModel.state.value.activePresets.first { it.id == "nav_hints" }
        assertTrue(initialNavPreset.isEnabled)

        viewModel.togglePreset("nav_hints", false)
        val updatedNavPreset = viewModel.state.value.activePresets.first { it.id == "nav_hints" }
        assertFalse(updatedNavPreset.isEnabled)
    }
}

