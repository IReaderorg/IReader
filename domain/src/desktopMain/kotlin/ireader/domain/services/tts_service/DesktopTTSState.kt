package ireader.domain.services.tts_service

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.source.Source
import ireader.core.source.model.Text
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.models.prefs.IReaderVoice
import ireader.domain.services.tts_service.piper.DownloadProgress
import ireader.domain.services.tts_service.piper.VoiceModel
import ireader.domain.services.tts_service.piper.WordBoundary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.ExperimentalTime

/**
 * Desktop implementation of TTS state
 */
class DesktopTTSState : TTSState {
    private val _isServiceConnected = MutableStateFlow(false)
    override val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()
    
    private val _currentReadingParagraph = MutableStateFlow(0)
    override val currentReadingParagraph: StateFlow<Int> = _currentReadingParagraph.asStateFlow()
    
    private val _previousReadingParagraph = MutableStateFlow(0)
    override val previousReadingParagraph: StateFlow<Int> = _previousReadingParagraph.asStateFlow()
    
    private val _currentVoice = MutableStateFlow<IReaderVoice?>(null)
    override val currentVoice: StateFlow<IReaderVoice?> = _currentVoice.asStateFlow()
    
    private val _prevVoice = MutableStateFlow<IReaderVoice?>(null)
    override val prevVoice: StateFlow<IReaderVoice?> = _prevVoice.asStateFlow()
    
    private val _currentLanguage = MutableStateFlow("")
    override val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()
    
    private val _prevLanguage = MutableStateFlow("")
    override val prevLanguage: StateFlow<String> = _prevLanguage.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _ttsChapter = MutableStateFlow<Chapter?>(null)
    override val ttsChapter: StateFlow<Chapter?> = _ttsChapter.asStateFlow()
    
    private val _ttsContent = MutableStateFlow<List<String>?>(null)
    override val ttsContent: StateFlow<List<String>?> = _ttsContent.asStateFlow()
    
    private val _autoNextChapter = MutableStateFlow(false)
    override val autoNextChapter: StateFlow<Boolean> = _autoNextChapter.asStateFlow()
    
    private val _translatedTTSContent = MutableStateFlow<List<String>?>(null)
    override val translatedTTSContent: StateFlow<List<String>?> = _translatedTTSContent.asStateFlow()
    
    private val _pitch = MutableStateFlow(0.8f)
    override val pitch: StateFlow<Float> = _pitch.asStateFlow()
    
    private val _prevPitch = MutableStateFlow(0.8f)
    override val prevPitch: StateFlow<Float> = _prevPitch.asStateFlow()
    
    private val _speechSpeed = MutableStateFlow(0.8f)
    override val speechSpeed: StateFlow<Float> = _speechSpeed.asStateFlow()
    
    private val _sleepTime = MutableStateFlow(0L)
    override val sleepTime: StateFlow<Long> = _sleepTime.asStateFlow()
    
    @OptIn(ExperimentalTime::class)
    private val _startTime = MutableStateFlow<kotlin.time.Instant?>(null)
    @OptIn(ExperimentalTime::class)
    override val startTime: StateFlow<kotlin.time.Instant?> = _startTime.asStateFlow()
    
    private val _sleepMode = MutableStateFlow(false)
    override val sleepMode: StateFlow<Boolean> = _sleepMode.asStateFlow()
    
    private val _isTTSReady = MutableStateFlow(true) // Desktop engines are ready after init
    override val isTTSReady: StateFlow<Boolean> = _isTTSReady.asStateFlow()
    
    private val _prevSpeechSpeed = MutableStateFlow(0.8f)
    override val prevSpeechSpeed: StateFlow<Float> = _prevSpeechSpeed.asStateFlow()
    
    private val _ttsBook = MutableStateFlow<Book?>(null)
    override val ttsBook: StateFlow<Book?> = _ttsBook.asStateFlow()
    
    private val _ttsCatalog = MutableStateFlow<CatalogLocal?>(null)
    override val ttsCatalog: StateFlow<CatalogLocal?> = _ttsCatalog.asStateFlow()
    
    private val _ttsSource = MutableStateFlow<Source?>(null)
    override val ttsSource: StateFlow<Source?> = _ttsSource.asStateFlow()
    
    private val _ttsChapters = MutableStateFlow<List<Chapter>>(emptyList())
    override val ttsChapters: StateFlow<List<Chapter>> = _ttsChapters.asStateFlow()
    
    private val _ttsCurrentChapterIndex = MutableStateFlow(-1)
    override val ttsCurrentChapterIndex: StateFlow<Int> = _ttsCurrentChapterIndex.asStateFlow()
    
    private val _utteranceId = MutableStateFlow("")
    override val utteranceId: StateFlow<String> = _utteranceId.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _uiChapters = MutableStateFlow<List<Chapter>>(emptyList())
    override val uiChapters: StateFlow<List<Chapter>> = _uiChapters.asStateFlow()
    
    private val _isDrawerAsc = MutableStateFlow(false)
    override val isDrawerAsc: StateFlow<Boolean> = _isDrawerAsc.asStateFlow()
    
    // Piper-specific properties
    /** Current word boundary for text highlighting during TTS playback */
    var currentWordBoundary by mutableStateOf<WordBoundary?>(null)
    
    /** List of available Piper voice models */
    var availableVoiceModels by mutableStateOf<List<VoiceModel>>(emptyList())
    
    /** Currently selected Piper voice model */
    var selectedVoiceModel by mutableStateOf<VoiceModel?>(null)
    
    /** Indicates if a voice model is currently being downloaded */
    var isDownloadingModel by mutableStateOf(false)
    
    /** Download progress for the current model download */
    var downloadProgress by mutableStateOf<DownloadProgress?>(null)
    
    /** Indicates if the source extension is not installed (for showing warning) */
    var sourceNotInstalledError by mutableStateOf(false)
    
    // Setter implementations
    override fun setPlaying(value: Boolean) { _isPlaying.value = value }
    override fun setTtsContent(value: List<String>?) { _ttsContent.value = value }
    override fun setTranslatedTTSContent(value: List<String>?) { _translatedTTSContent.value = value }
    override fun setCurrentReadingParagraph(value: Int) { _currentReadingParagraph.value = value }
    override fun setPreviousReadingParagraph(value: Int) { _previousReadingParagraph.value = value }
    override fun setAutoNextChapter(value: Boolean) { _autoNextChapter.value = value }
    override fun setCurrentVoice(value: IReaderVoice?) { _currentVoice.value = value }
    override fun setPrevVoice(value: IReaderVoice?) { _prevVoice.value = value }
    override fun setCurrentLanguage(value: String) { _currentLanguage.value = value }
    override fun setUtteranceId(value: String) { _utteranceId.value = value }
    override fun setPrevLanguage(value: String) { _prevLanguage.value = value }
    override fun setPitch(value: Float) { _pitch.value = value }
    override fun setPrevPitch(value: Float) { _prevPitch.value = value }
    override fun setSpeechSpeed(value: Float) { _speechSpeed.value = value }
    override fun setPrevSpeechSpeed(value: Float) { _prevSpeechSpeed.value = value }
    override fun setSleepTime(value: Long) { _sleepTime.value = value }
    override fun setTtsBook(value: Book?) { _ttsBook.value = value }
    override fun setTtsChapter(value: Chapter?) { 
        _ttsChapter.value = value
        // Update ttsContent when chapter changes
        updateTtsContent()
    }
    override fun setTtsCatalog(value: CatalogLocal?) { 
        _ttsCatalog.value = value
        _ttsSource.value = value?.source
    }
    override fun setTtsChapters(value: List<Chapter>) { 
        _ttsChapters.value = value
        updateUiChapters()
    }
    override fun setTtsCurrentChapterIndex(value: Int) { _ttsCurrentChapterIndex.value = value }
    override fun setServiceConnected(value: Boolean) { _isServiceConnected.value = value }
    override fun setLoading(value: Boolean) { _isLoading.value = value }
    override fun setUiChapters(value: List<Chapter>) { _uiChapters.value = value }
    override fun setDrawerAsc(value: Boolean) { 
        _isDrawerAsc.value = value
        updateUiChapters()
    }
    @OptIn(ExperimentalTime::class)
    override fun setStartTime(value: kotlin.time.Instant?) { _startTime.value = value }
    override fun setSleepMode(value: Boolean) { _sleepMode.value = value }
    override fun setTTSReady(value: Boolean) { _isTTSReady.value = value }
    
    private fun updateTtsContent() {
        val chapter = _ttsChapter.value
        _ttsContent.value = when {
            chapter == null -> null
            chapter.content.isEmpty() -> emptyList()
            // If content is Text objects
            chapter.content.firstOrNull() is Text -> {
                chapter.content
                    .filterIsInstance<Text>()
                    .map { it.text }
                    .filter { it.isNotBlank() }
                    .map { it.trim() }
            }
            // Otherwise treat as generic content
            else -> {
                chapter.content
                    .map { 
                        when (it) {
                            is Text -> it.text
                            else -> it.toString()
                        }
                    }
                    .filter { it.isNotBlank() }
                    .map { it.trim() }
            }
        }
    }
    
    private fun updateUiChapters() {
        _uiChapters.value = if (!_isDrawerAsc.value) _ttsChapters.value else _ttsChapters.value.reversed()
    }
}
