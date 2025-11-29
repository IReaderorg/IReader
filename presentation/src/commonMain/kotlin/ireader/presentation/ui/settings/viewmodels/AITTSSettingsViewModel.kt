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
    val totalDownloadedSize: Long = 0L
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
            
            updateState { it.copy(
                useAITTS = useAITTS,
                selectedProvider = selectedProvider,
                selectedVoiceId = selectedVoiceId,
                downloadedVoices = downloadedVoices,
                totalDownloadedSize = totalSize
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
}
