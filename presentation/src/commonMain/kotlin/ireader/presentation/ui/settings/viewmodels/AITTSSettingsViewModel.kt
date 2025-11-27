package ireader.presentation.ui.settings.viewmodels

import ireader.core.log.Log
import ireader.domain.models.tts.VoiceModel
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.services.tts.AITTSManager
import ireader.domain.services.tts.AITTSProvider
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.launch

data class AITTSSettingsState(
    val useAITTS: Boolean = false,
    val selectedProvider: AITTSProvider = AITTSProvider.NATIVE_ANDROID,
    val availableVoices: List<VoiceModel> = emptyList(),
    val selectedVoiceId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val downloadingVoice: String? = null,
    val downloadProgress: Int = 0,
    val downloadedVoices: Set<String> = emptySet(),
    val totalDownloadedSize: Long = 0L,

    // Coqui TTS
    val useCoquiTTS: Boolean = false,
    val coquiSpaceUrl: String = "https://kazemcodes-ireader.hf.space",
    val coquiApiKey: String = "",
    val coquiSpeed: Float = 1.0f,
    val isCoquiAvailable: Boolean = false
)

class AITTSSettingsViewModel(
    private val aiTTSManager: AITTSManager,
    private val appPreferences: AppPreferences
) : StateViewModel<AITTSSettingsState>(AITTSSettingsState()) {
    
    init {
        loadSettings()
        loadVoices()
    }
    
    private fun loadSettings() {
        scope.launch {
            val useAITTS = appPreferences.useAITTS().get()
            val selectedProvider = try {
                AITTSProvider.valueOf(appPreferences.selectedAITTSProvider().get())
            } catch (e: Exception) {
                AITTSProvider.NATIVE_ANDROID
            }
            val selectedVoiceId = appPreferences.selectedAIVoiceId().get()
            
            // Load downloaded voices info (will be empty on Android)
            val downloadedVoices = aiTTSManager.getDownloadedVoices().toSet()
            val totalSize = aiTTSManager.getDownloadedVoicesSize()

            // Load Coqui settings
            val useCoquiTTS = appPreferences.useCoquiTTS().get()
            val coquiSpaceUrl = appPreferences.coquiSpaceUrl().get()
            val coquiApiKey = appPreferences.coquiApiKey().get()
            val coquiSpeed = appPreferences.coquiSpeed().get()
            
            updateState { it.copy(
                useAITTS = useAITTS,
                selectedProvider = selectedProvider,
                selectedVoiceId = selectedVoiceId,
                downloadedVoices = downloadedVoices,
                totalDownloadedSize = totalSize,
                useCoquiTTS = useCoquiTTS,
                coquiSpaceUrl = coquiSpaceUrl,
                coquiApiKey = coquiApiKey,
                coquiSpeed = coquiSpeed
            ) }
            
            // Don't try to load voices on Android - not supported
        }
    }
    
    fun setUseAITTS(enabled: Boolean) {
        scope.launch {
            appPreferences.useAITTS().set(enabled)
            updateState { it.copy(useAITTS = enabled) }
            
            // Don't try to load voices on Android - not supported
        }
    }
    
    fun loadVoices() {
        scope.launch {
            updateState { it.copy(isLoading = true) }
            
            try {
                // Load voices from PiperVoiceCatalog
                val voices = ireader.domain.catalogs.PiperVoiceCatalog.getAllVoices()
                
                updateState { it.copy(
                    isLoading = false,
                    error = null,
                    availableVoices = voices
                ) }
            } catch (e: Exception) {
                Log.error { "Failed to load voices: ${e.message}" }
                updateState { it.copy(
                    isLoading = false,
                    error = "Failed to load voices: ${e.message}",
                    availableVoices = emptyList()
                ) }
            }
        }
    }
    
    fun selectVoice(voiceId: String) {
        updateState { it.copy(selectedVoiceId = voiceId) }
        scope.launch {
            appPreferences.selectedAIVoiceId().set(voiceId)
        }
    }
    
    fun previewVoice(voiceId: String) {
        // Voice preview not supported on Android
        // Users can test voices in Android Settings or Sherpa TTS app
        Log.info { "Voice preview not available on Android" }
    }
    
    fun downloadVoice(voice: VoiceModel) {
        // Voice download not supported on Android
        // Users should download voices in Sherpa TTS app
        Log.info { "Voice download not available on Android - use Sherpa TTS app" }
    }
    
    fun deleteVoice(voiceId: String) {
        // Voice deletion not supported on Android
        // Users should manage voices in Sherpa TTS app
        Log.info { "Voice deletion not available on Android - use Sherpa TTS app" }
    }

    
    // Coqui TTS Methods
    
    private fun configureCoqui() {
        scope.launch {
            try {
                val spaceUrl = appPreferences.coquiSpaceUrl().get()
                val apiKey = appPreferences.coquiApiKey().get()
                
                if (spaceUrl.isNotEmpty()) {
                    aiTTSManager.configureCoqui(spaceUrl, apiKey.ifEmpty { null })
                    updateState { it.copy(isCoquiAvailable = true) }
                    Log.info { "Configured Coqui TTS: $spaceUrl" }
                }
            } catch (e: Exception) {
                Log.error { "Failed to configure Coqui TTS: ${e.message}" }
                updateState { it.copy(isCoquiAvailable = false) }
            }
        }
    }
    
    fun setUseCoquiTTS(enabled: Boolean) {
        scope.launch {
            appPreferences.useCoquiTTS().set(enabled)
            updateState { it.copy(useCoquiTTS = enabled) }
            
            if (enabled) {
                configureCoqui()
            }
        }
    }
    
    fun setCoquiSpaceUrl(url: String) {
        scope.launch {
            appPreferences.coquiSpaceUrl().set(url)
            updateState { it.copy(coquiSpaceUrl = url) }
            
            // Reconfigure if enabled
            if (state.value.useCoquiTTS && url.isNotEmpty()) {
                configureCoqui()
            }
        }
    }
    
    fun setCoquiApiKey(apiKey: String) {
        scope.launch {
            appPreferences.coquiApiKey().set(apiKey)
            updateState { it.copy(coquiApiKey = apiKey) }
            
            // Reconfigure if enabled
            if (state.value.useCoquiTTS) {
                configureCoqui()
            }
        }
    }
    
    fun setCoquiSpeed(speed: Float) {
        scope.launch {
            val clampedSpeed = speed.coerceIn(0.5f, 2.0f)
            appPreferences.coquiSpeed().set(clampedSpeed)
            updateState { it.copy(coquiSpeed = clampedSpeed) }
        }
    }
    
    fun testCoquiTTS() {
        scope.launch {
            try {
                updateState { it.copy(isLoading = true, error = null) }
                
                aiTTSManager.synthesizeAndPlay(
                    text = "Hello! This is a test of Coqui TTS from your Hugging Face Space.",
                    provider = AITTSProvider.COQUI_TTS,
                    voiceId = "default",
                    speed = state.value.coquiSpeed
                ).onSuccess {
                    Log.info { "Coqui TTS test successful" }
                    updateState { it.copy(isLoading = false, error = null) }
                }.onFailure { error ->
                    Log.error { "Coqui TTS test failed: ${error.message}" }
                    updateState { it.copy(
                        isLoading = false,
                        error = "Test failed: ${error.message}"
                    ) }
                }
            } catch (e: Exception) {
                Log.error { "Failed to test Coqui TTS: ${e.message}" }
                updateState { it.copy(
                    isLoading = false,
                    error = "Test failed: ${e.message}"
                ) }
            }
        }
    }
}
