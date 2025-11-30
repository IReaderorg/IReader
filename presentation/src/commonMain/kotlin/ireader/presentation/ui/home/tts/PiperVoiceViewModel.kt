package ireader.presentation.ui.home.tts

import ireader.core.log.Log
import ireader.domain.models.tts.PiperVoice
import ireader.domain.services.tts_service.PiperVoiceDownloader
import ireader.domain.services.tts_service.PiperVoiceService
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Piper Voice Selection UI.
 * Manages voice catalog, filtering, and download state.
 */
class PiperVoiceViewModel(
    private val voiceService: PiperVoiceService,
    private val voiceDownloader: PiperVoiceDownloader? = null // Desktop-only
) : BaseViewModel() {

    // All voices from database
    private val _allVoices = voiceService.subscribeAll()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    // Filter state
    private val _filterLanguage = MutableStateFlow<String?>(null)
    val filterLanguage: StateFlow<String?> = _filterLanguage.asStateFlow()

    // Selected voice ID
    private val _selectedVoiceId = MutableStateFlow<String?>(null)
    val selectedVoiceId: StateFlow<String?> = _selectedVoiceId.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Download state
    private val _downloadingVoiceId = MutableStateFlow<String?>(null)
    val downloadingVoiceId: StateFlow<String?> = _downloadingVoiceId.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _downloadError = MutableStateFlow<String?>(null)
    val downloadError: StateFlow<String?> = _downloadError.asStateFlow()

    // Refreshing state from service
    val isRefreshing: StateFlow<Boolean> = voiceService.isRefreshing
    val refreshError: StateFlow<String?> = voiceService.refreshError

    // Filtered voices
    val voices: StateFlow<List<PiperVoice>> = combine(
        _allVoices,
        _filterLanguage
    ) { voices, language ->
        if (language == null) voices
        else voices.filter { it.language == language }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    // Available languages
    val availableLanguages: StateFlow<List<String>> = _allVoices
        .combine(MutableStateFlow(Unit)) { voices, _ ->
            voices.map { it.language }.distinct().sorted()
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    init {
        initialize()
    }

    private fun initialize() {
        scope.launch {
            _isLoading.value = true
            try {
                voiceService.initialize()
            } catch (e: Exception) {
                Log.error { "[PiperVoiceViewModel] Initialize failed: ${e.message}" }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setFilterLanguage(language: String?) {
        _filterLanguage.value = language
    }

    fun setSelectedVoice(voiceId: String?) {
        _selectedVoiceId.value = voiceId
    }

    fun refresh() {
        scope.launch {
            voiceService.refresh()
        }
    }

    fun downloadVoice(voice: PiperVoice) {
        val downloader = voiceDownloader ?: return

        scope.launch {
            _downloadingVoiceId.value = voice.id
            _downloadProgress.value = 0f
            _downloadError.value = null

            try {
                downloader.downloadVoice(voice) { progress ->
                    _downloadProgress.value = progress
                }

                // Mark as downloaded in database
                voiceService.markAsDownloaded(voice.id)

                Log.info { "[PiperVoiceViewModel] Voice ${voice.id} downloaded successfully" }
            } catch (e: Exception) {
                Log.error { "[PiperVoiceViewModel] Download failed: ${e.message}" }
                _downloadError.value = "Download failed: ${e.message}"
            } finally {
                _downloadingVoiceId.value = null
            }
        }
    }

    fun dismissError() {
        _downloadError.value = null
    }
}
