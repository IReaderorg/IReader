package ireader.presentation.ui.settings.audio

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
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

class AudioStudioViewModelTest {

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
    fun testEngineSwitchingAndPreferences() {
        val prefStore = MockPreferenceStore()
        val readerPrefs = ReaderPreferences(prefStore)
        val appPrefs = AppPreferences(prefStore)

        val viewModel = AudioStudioViewModel(readerPrefs, appPrefs)

        assertEquals(AudioEngineType.PIPER_NEURAL, viewModel.state.value.selectedEngine)
        assertEquals(3, viewModel.state.value.availableEngines.size)

        viewModel.setEngine(AudioEngineType.GRADIO_AI)
        assertEquals(AudioEngineType.GRADIO_AI, viewModel.state.value.selectedEngine)
        assertTrue(appPrefs.useAITTS().get())
        assertTrue(appPrefs.useGradioTTS().get())

        viewModel.setEngine(AudioEngineType.PIPER_NEURAL)
        assertEquals(AudioEngineType.PIPER_NEURAL, viewModel.state.value.selectedEngine)
        assertFalse(appPrefs.useAITTS().get())
    }

    @Test
    fun testSpeedAndPitchClamping() {
        val prefStore = MockPreferenceStore()
        val readerPrefs = ReaderPreferences(prefStore)
        val appPrefs = AppPreferences(prefStore)

        val viewModel = AudioStudioViewModel(readerPrefs, appPrefs)

        // Speed clamping (0.5 to 3.0)
        viewModel.setSpeechRate(0.1f)
        assertEquals(0.5f, viewModel.state.value.speechRate)

        viewModel.setSpeechRate(5.0f)
        assertEquals(3.0f, viewModel.state.value.speechRate)

        viewModel.setSpeechRate(1.5f)
        assertEquals(1.5f, viewModel.state.value.speechRate)

        // Pitch clamping (0.5 to 2.0)
        viewModel.setSpeechPitch(0.2f)
        assertEquals(0.5f, viewModel.state.value.speechPitch)

        viewModel.setSpeechPitch(4.0f)
        assertEquals(2.0f, viewModel.state.value.speechPitch)

        viewModel.resetRateAndPitch()
        assertEquals(1.0f, viewModel.state.value.speechRate)
        assertEquals(1.0f, viewModel.state.value.speechPitch)
    }

    @Test
    fun testPlaybackTogglesAndSleepTimer() {
        val prefStore = MockPreferenceStore()
        val readerPrefs = ReaderPreferences(prefStore)
        val appPrefs = AppPreferences(prefStore)

        val viewModel = AudioStudioViewModel(readerPrefs, appPrefs)

        viewModel.toggleAutoNext(false)
        assertFalse(viewModel.state.value.autoNextChapter)
        assertFalse(readerPrefs.autoNextChapter().get())

        viewModel.toggleAutoNext(true)
        assertTrue(viewModel.state.value.autoNextChapter)
        assertTrue(readerPrefs.autoNextChapter().get())

        viewModel.setSleepTimer(45)
        assertEquals(45, viewModel.state.value.sleepTimerMinutes)
        assertEquals(45L, readerPrefs.sleepTime().get())
    }

    @Test
    fun testTextMergingAndCaching() {
        val prefStore = MockPreferenceStore()
        val readerPrefs = ReaderPreferences(prefStore)
        val appPrefs = AppPreferences(prefStore)

        val viewModel = AudioStudioViewModel(readerPrefs, appPrefs)

        viewModel.setMergeWordsRemote(60)
        assertEquals(60, viewModel.state.value.mergeWordsRemote)
        assertEquals(60, readerPrefs.ttsMergeWordsRemote().get())

        viewModel.setMergeWordsNative(30)
        assertEquals(30, viewModel.state.value.mergeWordsNative)
        assertEquals(30, readerPrefs.ttsMergeWordsNative().get())

        viewModel.setChapterCacheEnabled(true)
        assertTrue(viewModel.state.value.chapterCacheEnabled)
        assertTrue(readerPrefs.ttsChapterCacheEnabled().get())

        viewModel.setChapterCacheDays(14)
        assertEquals(14, viewModel.state.value.chapterCacheDays)
        assertEquals(14, readerPrefs.ttsChapterCacheDays().get())
    }

    @Test
    fun testCloudAISamplePlaybackToggle() {
        val prefStore = MockPreferenceStore()
        val readerPrefs = ReaderPreferences(prefStore)
        val appPrefs = AppPreferences(prefStore)

        val viewModel = AudioStudioViewModel(readerPrefs, appPrefs)
        viewModel.setEngine(AudioEngineType.GRADIO_AI)
        assertEquals(AudioEngineType.GRADIO_AI, viewModel.state.value.selectedEngine)

        viewModel.setSampleText("This is a custom test preview.")
        assertEquals("This is a custom test preview.", viewModel.state.value.sampleText)

        // Starting sample playback
        viewModel.togglePlaySample()
        assertTrue(viewModel.state.value.isPlayingSample)

        // Stopping sample playback
        viewModel.togglePlaySample()
        assertFalse(viewModel.state.value.isPlayingSample)
    }
}
