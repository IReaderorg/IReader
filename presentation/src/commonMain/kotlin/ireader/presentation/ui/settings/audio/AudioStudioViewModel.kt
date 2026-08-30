package ireader.presentation.ui.settings.audio

import androidx.compose.runtime.Stable
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AudioEngineType {
    DEVICE_TTS,
    PIPER_NEURAL,
    GRADIO_AI
}

@Stable
data class AudioStudioState(
    val selectedEngine: AudioEngineType = AudioEngineType.DEVICE_TTS,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val selectedVoiceName: String = "Default Device Voice",
    val availableVoices: List<String> = listOf("Default Device Voice", "English (United States)", "English (United Kingdom)", "Neural Male", "Neural Female"),
    val autoNextChapter: Boolean = true,
    val autoScrollWithSpeech: Boolean = true,
    val skipBlankLines: Boolean = true,
    val sleepTimerMinutes: Int = 0,
    val isPlayingSample: Boolean = false,
    val sampleText: String = "The quick brown fox jumps over the lazy dog.",
    val highlightColorHex: Long = 0xFF6200EE
)

class AudioStudioViewModel(
    private val readerPreferences: ReaderPreferences,
    private val appPreferences: AppPreferences
) : StateViewModel<AudioStudioState>(AudioStudioState()) {

    private var samplePlaybackJob: Job? = null

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val rate = readerPreferences.speechRate().get()
        val pitch = readerPreferences.speechPitch().get()
        val autoNext = readerPreferences.autoNextChapter().get()
        val sleepTimer = readerPreferences.sleepTime().get().toInt()
        val voice = readerPreferences.speechVoice().get()

        val useAI = appPreferences.useAITTS().get()
        val engine = if (useAI) AudioEngineType.GRADIO_AI else AudioEngineType.DEVICE_TTS

        updateState {
            it.copy(
                selectedEngine = engine,
                speechRate = if (rate > 0f) rate else 1.0f,
                speechPitch = if (pitch > 0f) pitch else 1.0f,
                selectedVoiceName = if (voice.isNotBlank()) voice else "Default Device Voice",
                autoNextChapter = autoNext,
                sleepTimerMinutes = sleepTimer
            )
        }
    }

    fun setEngine(engine: AudioEngineType) {
        appPreferences.useAITTS().set(engine == AudioEngineType.GRADIO_AI)
        updateState { it.copy(selectedEngine = engine) }
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.5f, 3.0f)
        readerPreferences.speechRate().set(clamped)
        updateState { it.copy(speechRate = clamped) }
    }

    fun setSpeechPitch(pitch: Float) {
        val clamped = pitch.coerceIn(0.5f, 2.0f)
        readerPreferences.speechPitch().set(clamped)
        updateState { it.copy(speechPitch = clamped) }
    }

    fun setVoice(voice: String) {
        readerPreferences.speechVoice().set(voice)
        updateState { it.copy(selectedVoiceName = voice) }
    }

    fun toggleAutoNext(enabled: Boolean) {
        readerPreferences.autoNextChapter().set(enabled)
        updateState { it.copy(autoNextChapter = enabled) }
    }

    fun toggleAutoScroll(enabled: Boolean) {
        updateState { it.copy(autoScrollWithSpeech = enabled) }
    }

    fun toggleSkipBlankLines(enabled: Boolean) {
        updateState { it.copy(skipBlankLines = enabled) }
    }

    fun setSleepTimer(minutes: Int) {
        readerPreferences.sleepTime().set(minutes.toLong())
        updateState { it.copy(sleepTimerMinutes = minutes) }
    }

    fun setSampleText(text: String) {
        updateState { it.copy(sampleText = text) }
    }

    fun togglePlaySample() {
        if (state.value.isPlayingSample) {
            samplePlaybackJob?.cancel()
            updateState { it.copy(isPlayingSample = false) }
        } else {
            samplePlaybackJob?.cancel()
            samplePlaybackJob = scope.launch {
                updateState { it.copy(isPlayingSample = true) }
                // Simulate sample playback duration based on rate
                val durationMs = (3000L / state.value.speechRate).toLong()
                delay(durationMs)
                updateState { it.copy(isPlayingSample = false) }
            }
        }
    }

    fun resetRateAndPitch() {
        setSpeechRate(1.0f)
        setSpeechPitch(1.0f)
    }
}
