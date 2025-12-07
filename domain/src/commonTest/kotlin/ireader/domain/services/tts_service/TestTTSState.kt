package ireader.domain.services.tts_service

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.core.source.Source
import ireader.domain.preferences.models.prefs.IReaderVoice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.ExperimentalTime

/**
 * Test implementation of TTSState for unit testing
 * 
 * This implementation uses MutableStateFlow for all state properties
 * to allow testing state changes without Android dependencies.
 */
class TestTTSState : TTSState {
    
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying
    
    private val _ttsContent = MutableStateFlow<List<String>?>(null)
    override val ttsContent: StateFlow<List<String>?> = _ttsContent
    
    private val _currentReadingParagraph = MutableStateFlow(0)
    override val currentReadingParagraph: StateFlow<Int> = _currentReadingParagraph
    
    private val _previousReadingParagraph = MutableStateFlow(0)
    override val previousReadingParagraph: StateFlow<Int> = _previousReadingParagraph
    
    private val _autoNextChapter = MutableStateFlow(false)
    override val autoNextChapter: StateFlow<Boolean> = _autoNextChapter
    
    private val _currentVoice = MutableStateFlow<IReaderVoice?>(null)
    override val currentVoice: StateFlow<IReaderVoice?> = _currentVoice
    
    private val _prevVoice = MutableStateFlow<IReaderVoice?>(null)
    override val prevVoice: StateFlow<IReaderVoice?> = _prevVoice
    
    private val _currentLanguage = MutableStateFlow("")
    override val currentLanguage: StateFlow<String> = _currentLanguage
    
    private val _utteranceId = MutableStateFlow("")
    override val utteranceId: StateFlow<String> = _utteranceId
    
    private val _prevLanguage = MutableStateFlow("")
    override val prevLanguage: StateFlow<String> = _prevLanguage
    
    private val _pitch = MutableStateFlow(1.0f)
    override val pitch: StateFlow<Float> = _pitch
    
    private val _prevPitch = MutableStateFlow(1.0f)
    override val prevPitch: StateFlow<Float> = _prevPitch
    
    private val _speechSpeed = MutableStateFlow(1.0f)
    override val speechSpeed: StateFlow<Float> = _speechSpeed
    
    private val _prevSpeechSpeed = MutableStateFlow(1.0f)
    override val prevSpeechSpeed: StateFlow<Float> = _prevSpeechSpeed
    
    private val _sleepTime = MutableStateFlow(0L)
    override val sleepTime: StateFlow<Long> = _sleepTime
    
    private val _ttsBook = MutableStateFlow<Book?>(null)
    override val ttsBook: StateFlow<Book?> = _ttsBook
    
    private val _ttsChapter = MutableStateFlow<Chapter?>(null)
    override val ttsChapter: StateFlow<Chapter?> = _ttsChapter
    
    private val _ttsSource = MutableStateFlow<Source?>(null)
    override val ttsSource: StateFlow<Source?> = _ttsSource
    
    private val _ttsCatalog = MutableStateFlow<CatalogLocal?>(null)
    override val ttsCatalog: StateFlow<CatalogLocal?> = _ttsCatalog
    
    private val _ttsChapters = MutableStateFlow<List<Chapter>>(emptyList())
    override val ttsChapters: StateFlow<List<Chapter>> = _ttsChapters
    
    private val _ttsCurrentChapterIndex = MutableStateFlow(-1)
    override val ttsCurrentChapterIndex: StateFlow<Int> = _ttsCurrentChapterIndex
    
    private val _isServiceConnected = MutableStateFlow(false)
    override val isServiceConnected: StateFlow<Boolean> = _isServiceConnected
    
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _uiChapters = MutableStateFlow<List<Chapter>>(emptyList())
    override val uiChapters: StateFlow<List<Chapter>> = _uiChapters
    
    private val _isDrawerAsc = MutableStateFlow(false)
    override val isDrawerAsc: StateFlow<Boolean> = _isDrawerAsc
    
    @OptIn(ExperimentalTime::class)
    private val _startTime = MutableStateFlow<kotlin.time.Instant?>(null)
    @OptIn(ExperimentalTime::class)
    override val startTime: StateFlow<kotlin.time.Instant?> = _startTime
    
    private val _sleepMode = MutableStateFlow(false)
    override val sleepMode: StateFlow<Boolean> = _sleepMode
    
    // Setter implementations
    override fun setPlaying(value: Boolean) { _isPlaying.value = value }
    override fun setTtsContent(value: List<String>?) { _ttsContent.value = value }
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
    override fun setTtsChapter(value: Chapter?) { _ttsChapter.value = value }
    override fun setTtsCatalog(value: CatalogLocal?) { _ttsCatalog.value = value }
    override fun setTtsChapters(value: List<Chapter>) { _ttsChapters.value = value }
    override fun setTtsCurrentChapterIndex(value: Int) { _ttsCurrentChapterIndex.value = value }
    override fun setServiceConnected(value: Boolean) { _isServiceConnected.value = value }
    override fun setLoading(value: Boolean) { _isLoading.value = value }
    override fun setUiChapters(value: List<Chapter>) { _uiChapters.value = value }
    override fun setDrawerAsc(value: Boolean) { _isDrawerAsc.value = value }
    @OptIn(ExperimentalTime::class)
    override fun setStartTime(value: kotlin.time.Instant?) { _startTime.value = value }
    override fun setSleepMode(value: Boolean) { _sleepMode.value = value }

    private val _translatedTTSContent = MutableStateFlow<List<String>?>(null)
    override val translatedTTSContent: StateFlow<List<String>?> = _translatedTTSContent
    override fun setTranslatedTTSContent(value: List<String>?) { _translatedTTSContent.value = value }
    
    private val _isTTSReady = MutableStateFlow(false)
    override val isTTSReady: StateFlow<Boolean> = _isTTSReady
    override fun setTTSReady(value: Boolean) { _isTTSReady.value = value }
}
