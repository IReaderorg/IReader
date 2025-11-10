package ireader.presentation.ui.settings.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import ireader.domain.catalogs.VoiceCatalog
import ireader.domain.models.tts.VoiceModel
import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for voice selection screen
 * Requirements: 4.1, 4.2, 4.3, 10.1
 */
class VoiceSelectionViewModel(
    private val uiPreferences: UiPreferences
) : StateScreenModel<VoiceSelectionState>(VoiceSelectionState()) {
    
    private val _downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    val downloadProgress: StateFlow<DownloadProgress?> = _downloadProgress.asStateFlow()
    
    private val _installedVoices = MutableStateFlow<Set<String>>(emptySet())
    val installedVoices: StateFlow<Set<String>> = _installedVoices.asStateFlow()
    
    init {
        loadVoices()
        loadInstalledVoices()
    }
    
    private fun loadVoices() {
        mutableState.update { currentState ->
            currentState.copy(
                allVoices = VoiceCatalog.getAllVoices(),
                filteredVoices = VoiceCatalog.getAllVoices(),
                supportedLanguages = VoiceCatalog.getSupportedLanguages()
            )
        }
    }
    
    private fun loadInstalledVoices() {
        screenModelScope.launch {
            // TODO: Load from actual storage when voice download is implemented
            _installedVoices.value = emptySet()
        }
    }
    
    fun filterByLanguage(language: String?) {
        mutableState.update { currentState ->
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
        mutableState.update { currentState ->
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
        mutableState.update { it.copy(selectedVoice = voice) }
        // Save to preferences
        screenModelScope.launch {
            uiPreferences.selectedVoiceId().set(voice.id)
        }
    }
    
    fun downloadVoice(voice: VoiceModel) {
        screenModelScope.launch {
            _downloadProgress.value = DownloadProgress(voice.id, 0f)
            
            try {
                // TODO: Implement actual download when voice repository is ready
                // Simulate download progress for now
                for (progress in 0..100 step 10) {
                    kotlinx.coroutines.delay(100)
                    _downloadProgress.value = DownloadProgress(voice.id, progress / 100f)
                }
                
                // Mark as installed
                _installedVoices.update { it + voice.id }
                _downloadProgress.value = null
                
            } catch (e: Exception) {
                _downloadProgress.value = null
                mutableState.update { it.copy(error = "Failed to download voice: ${e.message}") }
            }
        }
    }
    
    fun previewVoice(voice: VoiceModel) {
        screenModelScope.launch {
            try {
                // TODO: Implement voice preview when TTS service is ready
                // For now, just select the voice
                selectVoice(voice)
            } catch (e: Exception) {
                mutableState.update { it.copy(error = "Failed to preview voice: ${e.message}") }
            }
        }
    }
    
    fun deleteVoice(voice: VoiceModel) {
        screenModelScope.launch {
            try {
                // TODO: Implement actual deletion when voice storage is ready
                _installedVoices.update { it - voice.id }
            } catch (e: Exception) {
                mutableState.update { it.copy(error = "Failed to delete voice: ${e.message}") }
            }
        }
    }
    
    fun isVoiceDownloaded(voiceId: String): Boolean {
        return _installedVoices.value.contains(voiceId)
    }
    
    fun clearError() {
        mutableState.update { it.copy(error = null) }
    }
}

/**
 * State for voice selection screen
 */
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
data class DownloadProgress(
    val voiceId: String,
    val progress: Float // 0.0 to 1.0
)
