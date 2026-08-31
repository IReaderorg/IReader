package ireader.domain.usecases.translate

import ireader.core.http.HttpClients
import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.prefs.ReaderPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TranslationEnginesManagerTest {

    private class MockPreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, Any?>()

        override fun getString(key: String, defaultValue: String): Preference<String> =
            createMockPreference(key, defaultValue)

        override fun getLong(key: String, defaultValue: Long): Preference<Long> =
            createMockPreference(key, defaultValue)

        override fun getInt(key: String, defaultValue: Int): Preference<Int> =
            createMockPreference(key, defaultValue)

        override fun getFloat(key: String, defaultValue: Float): Preference<Float> =
            createMockPreference(key, defaultValue)

        override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> =
            createMockPreference(key, defaultValue)

        @Suppress("UNCHECKED_CAST")
        private fun <T> createMockPreference(key: String, defaultValue: T): Preference<T> {
            return object : Preference<T> {
                private val flow = MutableStateFlow(values[key] as? T ?: defaultValue)

                override fun key(): String = key
                override fun get(): T = (values[key] as? T) ?: defaultValue
                override fun set(value: T) {
                    values[key] = value
                    flow.value = value
                }
                override fun isSet(): Boolean = values.containsKey(key)
                override fun delete() {
                    values.remove(key)
                    flow.value = defaultValue
                }
                override fun defaultValue(): T = defaultValue
                override fun changes(): Flow<T> = flow
                override fun stateIn(scope: kotlinx.coroutines.CoroutineScope): StateFlow<T> = flow
            }
        }

        override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> = throw UnsupportedOperationException()
        override fun <T> getObject(key: String, defaultValue: T, serializer: (T) -> String, deserializer: (String) -> T): Preference<T> = throw UnsupportedOperationException()
        override fun <T> getJsonObject(key: String, defaultValue: T, serializer: KSerializer<T>, serializersModule: SerializersModule): Preference<T> = throw UnsupportedOperationException()
    }

    @Test
    fun testAllBuiltInEnginesPresent() {
        val prefStore = MockPreferenceStore()
        val readerPrefs = ReaderPreferences(prefStore)
        val httpClients = HttpClients(prefStore)

        val manager = TranslationEnginesManager(readerPrefs, httpClients)

        val engines = manager.builtInEngines
        val engineIds = engines.map { it.id }

        // Assert all major engines are registered in builtInEngines
        assertTrue(engineIds.contains(0L), "Google ML Kit (0L) should be present")
        assertTrue(engineIds.contains(11L), "Google Translate Free (11L) should be present")
        assertTrue(engineIds.contains(12L), "Gemini Nano (12L) should be present")
        assertTrue(engineIds.contains(13L), "Claude AI (13L) should be present")
        assertTrue(engineIds.contains(8L), "Gemini AI (8L) should be present")
        assertTrue(engineIds.contains(2L), "OpenAI (2L) should be present")
        assertTrue(engineIds.contains(3L), "DeepSeek AI (3L) should be present")
        assertTrue(engineIds.contains(9L), "OpenRouter AI (9L) should be present")
        assertTrue(engineIds.contains(10L), "NVIDIA NIM (10L) should be present")
        assertTrue(engineIds.contains(5L), "Ollama (5L) should be present")
        assertTrue(engineIds.contains(4L), "LibreTranslate (4L) should be present")
        assertTrue(engineIds.contains(7L), "Free AI (7L) should be present")
    }

    @Test
    fun testEngineRetrievalAndSync() {
        val prefStore = MockPreferenceStore()
        val readerPrefs = ReaderPreferences(prefStore)
        val httpClients = HttpClients(prefStore)

        val manager = TranslationEnginesManager(readerPrefs, httpClients)

        // Select Claude (13L)
        readerPrefs.translatorEngine().set(13L)
        val selectedClaude = manager.get()
        assertEquals(13L, selectedClaude.id)
        assertEquals("Claude AI (Anthropic)", selectedClaude.engineName)

        // Select DeepSeek (3L)
        readerPrefs.translatorEngine().set(3L)
        val selectedDeepSeek = manager.get()
        assertEquals(3L, selectedDeepSeek.id)
        assertEquals("DeepSeek AI", selectedDeepSeek.engineName)

        // Select OpenAI (2L)
        readerPrefs.translatorEngine().set(2L)
        val selectedOpenAI = manager.get()
        assertEquals(2L, selectedOpenAI.id)
        assertEquals("OpenAI (GPT)", selectedOpenAI.engineName)
    }

    @Test
    fun testDefaultEngineIsGoogleTranslateFree() {
        val prefStore = MockPreferenceStore()
        val readerPrefs = ReaderPreferences(prefStore)
        val httpClients = HttpClients(prefStore)

        val manager = TranslationEnginesManager(readerPrefs, httpClients)

        // Without setting preference, default should resolve to Google Translate Free (11L)
        assertEquals(11L, readerPrefs.translatorEngine().get())
        assertEquals(11L, manager.getSelectedEngineId())
        assertEquals(11L, manager.get().id)
        assertEquals("Google Translate (Free)", manager.get().engineName)
    }

    @Test
    fun testApiKeyResolutionForAllAIEngines() {
        val prefStore = MockPreferenceStore()
        val readerPrefs = ReaderPreferences(prefStore)
        val httpClients = HttpClients(prefStore)

        val manager = TranslationEnginesManager(readerPrefs, httpClients)

        // Test Claude API Key
        readerPrefs.translatorEngine().set(13L)
        readerPrefs.claudeApiKey().set("sk-ant-claude-test-key")
        assertEquals("sk-ant-claude-test-key", manager.getApiKeyForCurrentEngine())

        // Test DeepSeek API Key
        readerPrefs.translatorEngine().set(3L)
        readerPrefs.deepSeekApiKey().set("sk-deepseek-test-key")
        assertEquals("sk-deepseek-test-key", manager.getApiKeyForCurrentEngine())

        // Test OpenAI API Key
        readerPrefs.translatorEngine().set(2L)
        readerPrefs.openAIApiKey().set("sk-openai-test-key")
        assertEquals("sk-openai-test-key", manager.getApiKeyForCurrentEngine())

        // Test Gemini API Key
        readerPrefs.translatorEngine().set(8L)
        readerPrefs.geminiApiKey().set("gemini-test-key")
        assertEquals("gemini-test-key", manager.getApiKeyForCurrentEngine())

        // Test OpenRouter API Key
        readerPrefs.translatorEngine().set(9L)
        readerPrefs.openRouterApiKey().set("sk-or-test-key")
        assertEquals("sk-or-test-key", manager.getApiKeyForCurrentEngine())

        // Test NVIDIA NIM API Key
        readerPrefs.translatorEngine().set(10L)
        readerPrefs.nvidiaApiKey().set("nvapi-test-key")
        assertEquals("nvapi-test-key", manager.getApiKeyForCurrentEngine())
    }
}
