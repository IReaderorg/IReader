package ireader.presentation.ui.settings.viewmodels

import androidx.compose.runtime.Stable
import ireader.domain.catalogs.VoiceCatalog
import ireader.domain.models.tts.VoiceModel
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for voice selection screen
 * Requirements: 4.1, 4.2, 4.3, 10.1
 */
class VoiceSelectionViewModel(
    private val uiPreferences: UiPreferences,
    private val voiceStorage: ireader.domain.storage.VoiceStorage,
    private val voiceDownloader: ireader.domain.voice.VoiceDownloader,
    private val voicePreferences: ireader.domain.preferences.VoicePreferences
) : StateViewModel<VoiceSelectionState>(VoiceSelectionState()) {
    
    private val _downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    val downloadProgress: StateFlow<DownloadProgress?> = _downloadProgress.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)
    
    private val _installedVoices = MutableStateFlow<Set<String>>(emptySet())
    val installedVoices: StateFlow<Collection<String>> = _installedVoices.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptySet())
    
    init {
        loadVoices()
        loadInstalledVoices()
    }
    
    private fun loadVoices() {
        updateState { currentState ->
            currentState.copy(
                allVoices = VoiceCatalog.getAllVoices(),
                filteredVoices = VoiceCatalog.getAllVoices(),
                supportedLanguages = VoiceCatalog.getSupportedLanguages()
            )
        }
    }
    
    private fun loadInstalledVoices() {
        scope.launch {
            try {
                val installed = voiceStorage.getInstalledVoices()
                _installedVoices.value = installed.toSet()
                
                // Sync with preferences
                voicePreferences.setInstalledVoices(_installedVoices.value)
            } catch (e: Exception) {
                ireader.core.log.Log.error("Failed to load installed voices", e)
                _installedVoices.value = emptySet()
            }
        }
    }
    
    fun filterByLanguage(language: String?) {
        updateState { currentState ->
            val filtered = if (language == null) {
                currentState.allVoices
            } else {
                currentState.allVoices.filter { it.language == language }
            }
            currentState.copy(
                selectedLanguage = language,
                filteredVoices = filtered,
                searchQuery = ""
            )
        }
    }
    
    fun searchVoices(query: String) {
        updateState { currentState ->
            val filtered = if (query.isBlank()) {
                if (currentState.selectedLanguage != null) {
                    currentState.allVoices.filter { it.language == currentState.selectedLanguage }
                } else {
                    currentState.allVoices
                }
            } else {
                currentState.allVoices.filter { voice ->
                    voice.name.contains(query, ignoreCase = true) ||
                    voice.language.contains(query, ignoreCase = true) ||
                    voice.locale.contains(query, ignoreCase = true) ||
                    voice.tags.any { it.contains(query, ignoreCase = true) }
                }
            }
            currentState.copy(
                searchQuery = query,
                filteredVoices = filtered
            )
        }
    }
    
    fun selectVoice(voice: VoiceModel) {
        updateState { it.copy(selectedVoice = voice) }
        // Save to preferences
        scope.launch {
            uiPreferences.selectedVoiceId().set(voice.id)
        }
    }
    
    fun downloadVoice(voice: VoiceModel) {
        scope.launch {
            _downloadProgress.value = DownloadProgress(voice.id, 0f)
            
            try {
                val result = voiceDownloader.downloadVoice(voice) { progress ->
                    _downloadProgress.value = DownloadProgress(voice.id, progress)
                }
                
                result.onSuccess {
                    // Mark as installed
                    _installedVoices.update { it + voice.id }
                    voicePreferences.setInstalledVoices(_installedVoices.value)
                    _downloadProgress.value = null
                    
                    updateState { it.copy(error = null) }
                }.onFailure { error ->
                    _downloadProgress.value = null
                    updateState { it.copy(error = "Failed to download voice: ${error.message}") }
                }
            } catch (e: Exception) {
                _downloadProgress.value = null
                updateState { it.copy(error = "Failed to download voice: ${e.message}") }
            }
        }
    }
    
    fun previewVoice(voice: VoiceModel) {
        scope.launch {
            try {
                // Check if voice is installed
                if (!isVoiceDownloaded(voice.id)) {
                    updateState { it.copy(error = "Voice must be downloaded before preview") }
                    return@launch
                }
                
                // Select the voice
                selectVoice(voice)
                
                // Note: This ViewModel is for the voice catalog/download system.
                // Actual TTS voice selection is handled platform-specifically:
                // - Desktop: VoiceSelectionDialog directly calls DesktopTTSService.selectVoiceModel()
                // - Android: Uses Android TTS service APIs
                // Voice preview would require platform-specific TTS service integration
                updateState { it.copy(error = null) }
            } catch (e: Exception) {
                updateState { it.copy(error = "Failed to preview voice: ${e.message}") }
            }
        }
    }
    
    fun deleteVoice(voice: VoiceModel) {
        scope.launch {
            try {
                val result = voiceStorage.deleteVoiceModel(voice.id)
                
                result.onSuccess {
                    _installedVoices.update { it - voice.id }
                    voicePreferences.setInstalledVoices(_installedVoices.value)
                    
                    // If this was the selected voice, clear selection
                    if (state.value.selectedVoice?.id == voice.id) {
                        updateState { it.copy(selectedVoice = null) }
                    }
                    
                    updateState { it.copy(error = null) }
                }.onFailure { error ->
                    updateState { it.copy(error = "Failed to delete voice: ${error.message}") }
                }
            } catch (e: Exception) {
                updateState { it.copy(error = "Failed to delete voice: ${e.message}") }
            }
        }
    }
    
    fun isVoiceDownloaded(voiceId: String): Boolean {
        return _installedVoices.value.contains(voiceId)
    }
    
    fun clearError() {
        updateState { it.copy(error = null) }
    }
}

/**
 * State for voice selection screen
 */
@Stable
data class VoiceSelectionState(
    val allVoices: List<VoiceModel> = emptyList(),
    val filteredVoices: List<VoiceModel> = emptyList(),
    val selectedVoice: VoiceModel? = null,
    val selectedLanguage: String? = null,
    val supportedLanguages: List<String> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

/**
 * Download progress information
 */
@Stable
data class DownloadProgress(
    val voiceId: String,
    val progress: Float // 0.0 to 1.0
)
