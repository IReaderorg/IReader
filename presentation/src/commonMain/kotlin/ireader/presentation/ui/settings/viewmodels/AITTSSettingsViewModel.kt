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
    val selectedProvider: AITTSProvider = AITTSProvider.PIPER_TTS,
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
    }
    
    private fun loadSettings() {
        scope.launch {
            val useAITTS = appPreferences.useAITTS().get()
            val selectedProvider = try {
                AITTSProvider.valueOf(appPreferences.selectedAITTSProvider().get())
            } catch (e: Exception) {
                AITTSProvider.PIPER_TTS
            }
            val selectedVoiceId = appPreferences.selectedAIVoiceId().get()
            
            // Load downloaded voices info
            val downloadedVoices = aiTTSManager.getDownloadedVoices().toSet()
            val totalSize = aiTTSManager.getDownloadedVoicesSize()
            
            updateState { it.copy(
                useAITTS = useAITTS,
                selectedProvider = selectedProvider,
                selectedVoiceId = selectedVoiceId,
                downloadedVoices = downloadedVoices,
                totalDownloadedSize = totalSize
            ) }
            
            if (useAITTS) {
                loadVoices()
            }
        }
    }
    
    fun setUseAITTS(enabled: Boolean) {
        scope.launch {
            appPreferences.useAITTS().set(enabled)
            updateState { it.copy(useAITTS = enabled) }
            
            if (enabled) {
                loadVoices()
            }
        }
    }
    
    fun loadVoices() {
        scope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            
            try {
                // Always use Piper TTS
                val result = aiTTSManager.getVoicesFromProvider(AITTSProvider.PIPER_TTS)
                
                result.onSuccess { voices ->
                    updateState { it.copy(
                        availableVoices = voices,
                        isLoading = false,
                        error = null
                    ) }
                }.onFailure { e ->
                    Log.error { "Failed to load voices: ${e.message}" }
                    updateState { it.copy(
                        isLoading = false,
                        error = "Failed to load voices: ${e.message}"
                    ) }
                }
            } catch (e: Exception) {
                Log.error { "Error loading voices: ${e.message}" }
                updateState { it.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
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
        scope.launch {
            try {
                val sampleText = "Hello, this is a preview of my voice."
                
                // Always use Piper TTS
                aiTTSManager.synthesize(
                    text = sampleText,
                    provider = AITTSProvider.PIPER_TTS,
                    voiceId = voiceId,
                    speed = 1.0f,
                    pitch = 0.0f
                ).onSuccess {
                    // Audio will be played by the AITTSPlayer
                    Log.info { "Voice preview successful" }
                }.onFailure { e ->
                    Log.error { "Voice preview failed: ${e.message}" }
                    updateState { it.copy(error = "Preview failed: ${e.message}") }
                }
            } catch (e: Exception) {
                Log.error { "Error previewing voice: ${e.message}" }
                updateState { it.copy(error = "Error: ${e.message}") }
            }
        }
    }
    
    fun downloadVoice(voice: VoiceModel) {
        scope.launch {
            updateState { it.copy(
                downloadingVoice = voice.id,
                downloadProgress = 0,
                error = null
            ) }
            
            try {
                aiTTSManager.downloadPiperVoice(voice) { progress ->
                    updateState { it.copy(downloadProgress = progress) }
                }.onSuccess {
                    Log.info { "Voice downloaded successfully: ${voice.name}" }
                    
                    // Refresh downloaded voices list
                    val downloadedVoices = aiTTSManager.getDownloadedVoices().toSet()
                    val totalSize = aiTTSManager.getDownloadedVoicesSize()
                    
                    updateState { it.copy(
                        downloadingVoice = null,
                        downloadProgress = 0,
                        downloadedVoices = downloadedVoices,
                        totalDownloadedSize = totalSize
                    ) }
                }.onFailure { e ->
                    Log.error { "Failed to download voice: ${e.message}" }
                    updateState { it.copy(
                        downloadingVoice = null,
                        downloadProgress = 0,
                        error = "Download failed: ${e.message}"
                    ) }
                }
            } catch (e: Exception) {
                Log.error { "Error downloading voice: ${e.message}" }
                updateState { it.copy(
                    downloadingVoice = null,
                    downloadProgress = 0,
                    error = "Error: ${e.message}"
                ) }
            }
        }
    }
    
    fun deleteVoice(voiceId: String) {
        scope.launch {
            try {
                val success = aiTTSManager.deleteVoice(voiceId)
                if (success) {
                    Log.info { "Voice deleted successfully: $voiceId" }
                    
                    // Refresh downloaded voices list
                    val downloadedVoices = aiTTSManager.getDownloadedVoices().toSet()
                    val totalSize = aiTTSManager.getDownloadedVoicesSize()
                    
                    updateState { it.copy(
                        downloadedVoices = downloadedVoices,
                        totalDownloadedSize = totalSize
                    ) }
                } else {
                    updateState { it.copy(error = "Failed to delete voice") }
                }
            } catch (e: Exception) {
                Log.error { "Error deleting voice: ${e.message}" }
                updateState { it.copy(error = "Error: ${e.message}") }
            }
        }
    }
}
