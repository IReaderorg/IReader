package ireader.domain.services.tts_service

import android.speech.tts.Voice
import android.support.v4.media.MediaMetadataCompat
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.core.source.Source
import ireader.core.source.model.Text
import ireader.domain.preferences.models.prefs.IReaderVoice
import ireader.domain.services.tts_service.media_player.TTSService

import java.util.Locale
import kotlin.time.ExperimentalTime


/**
 * Android-specific TTS state interface with Android TTS features
 */
interface AndroidTTSState : ireader.domain.services.tts_service.TTSState {
    var languages: List<Locale>
    var voices: List<Voice>
    val uiVoices: List<IReaderVoice>
    var meta: MediaMetadataCompat?
}

class TTSStateImpl() : AndroidTTSState {
    private val _isServiceConnected = kotlinx.coroutines.flow.MutableStateFlow(false)
    override val isServiceConnected: kotlinx.coroutines.flow.StateFlow<Boolean> = _isServiceConnected
    
    private val _currentReadingParagraph = kotlinx.coroutines.flow.MutableStateFlow(0)
    override val currentReadingParagraph: kotlinx.coroutines.flow.StateFlow<Int> = _currentReadingParagraph
    
    private val _previousReadingParagraph = kotlinx.coroutines.flow.MutableStateFlow(0)
    override val previousReadingParagraph: kotlinx.coroutines.flow.StateFlow<Int> = _previousReadingParagraph
    
    override var languages by mutableStateOf<List<Locale>>(emptyList())
    override var voices by mutableStateOf<List<Voice>>(emptyList())
    override val uiVoices by derivedStateOf { voices.map { it.toIReaderVoice() } }
    
    private val _currentVoice = kotlinx.coroutines.flow.MutableStateFlow<IReaderVoice?>(null)
    override val currentVoice: kotlinx.coroutines.flow.StateFlow<IReaderVoice?> = _currentVoice
    
    private val _prevVoice = kotlinx.coroutines.flow.MutableStateFlow<IReaderVoice?>(null)
    override val prevVoice: kotlinx.coroutines.flow.StateFlow<IReaderVoice?> = _prevVoice
    
    private val _currentLanguage = kotlinx.coroutines.flow.MutableStateFlow("")
    override val currentLanguage: kotlinx.coroutines.flow.StateFlow<String> = _currentLanguage
    
    private val _prevLanguage = kotlinx.coroutines.flow.MutableStateFlow("")
    override val prevLanguage: kotlinx.coroutines.flow.StateFlow<String> = _prevLanguage
    
    private val _isPlaying = kotlinx.coroutines.flow.MutableStateFlow(false)
    override val isPlaying: kotlinx.coroutines.flow.StateFlow<Boolean> = _isPlaying
    
    private val _ttsChapter = kotlinx.coroutines.flow.MutableStateFlow<Chapter?>(null)
    override val ttsChapter: kotlinx.coroutines.flow.StateFlow<Chapter?> = _ttsChapter
    
    private val _ttsContent = kotlinx.coroutines.flow.MutableStateFlow<List<String>?>(null)
    override val ttsContent: kotlinx.coroutines.flow.StateFlow<List<String>?> = _ttsContent
    
    private val _translatedTTSContent = kotlinx.coroutines.flow.MutableStateFlow<List<String>?>(null)
    override val translatedTTSContent: kotlinx.coroutines.flow.StateFlow<List<String>?> = _translatedTTSContent
    
    private val _autoNextChapter = kotlinx.coroutines.flow.MutableStateFlow(false)
    override val autoNextChapter: kotlinx.coroutines.flow.StateFlow<Boolean> = _autoNextChapter
    
    private val _pitch = kotlinx.coroutines.flow.MutableStateFlow(0.8f)
    override val pitch: kotlinx.coroutines.flow.StateFlow<Float> = _pitch
    
    private val _prevPitch = kotlinx.coroutines.flow.MutableStateFlow(0.8f)
    override val prevPitch: kotlinx.coroutines.flow.StateFlow<Float> = _prevPitch
    
    private val _speechSpeed = kotlinx.coroutines.flow.MutableStateFlow(0.8f)
    override val speechSpeed: kotlinx.coroutines.flow.StateFlow<Float> = _speechSpeed
    
    private val _sleepTime = kotlinx.coroutines.flow.MutableStateFlow(0L)
    override val sleepTime: kotlinx.coroutines.flow.StateFlow<Long> = _sleepTime
    
    @OptIn(ExperimentalTime::class)
    private val _startTime = kotlinx.coroutines.flow.MutableStateFlow<kotlin.time.Instant?>(null)
    @OptIn(ExperimentalTime::class)
    override val startTime: kotlinx.coroutines.flow.StateFlow<kotlin.time.Instant?> = _startTime
    
    private val _sleepMode = kotlinx.coroutines.flow.MutableStateFlow(false)
    override val sleepMode: kotlinx.coroutines.flow.StateFlow<Boolean> = _sleepMode
    
    private val _isTTSReady = kotlinx.coroutines.flow.MutableStateFlow(false)
    override val isTTSReady: kotlinx.coroutines.flow.StateFlow<Boolean> = _isTTSReady
    
    private val _prevSpeechSpeed = kotlinx.coroutines.flow.MutableStateFlow(0.8f)
    override val prevSpeechSpeed: kotlinx.coroutines.flow.StateFlow<Float> = _prevSpeechSpeed
    
    private val _ttsBook = kotlinx.coroutines.flow.MutableStateFlow<Book?>(null)
    override val ttsBook: kotlinx.coroutines.flow.StateFlow<Book?> = _ttsBook
    
    private val _ttsCatalog = kotlinx.coroutines.flow.MutableStateFlow<CatalogLocal?>(null)
    override val ttsCatalog: kotlinx.coroutines.flow.StateFlow<CatalogLocal?> = _ttsCatalog
    
    private val _ttsSource = kotlinx.coroutines.flow.MutableStateFlow<Source?>(null)
    override val ttsSource: kotlinx.coroutines.flow.StateFlow<Source?> = _ttsSource
    
    private val _ttsChapters = kotlinx.coroutines.flow.MutableStateFlow<List<Chapter>>(emptyList())
    override val ttsChapters: kotlinx.coroutines.flow.StateFlow<List<Chapter>> = _ttsChapters
    
    private val _ttsCurrentChapterIndex = kotlinx.coroutines.flow.MutableStateFlow(-1)
    override val ttsCurrentChapterIndex: kotlinx.coroutines.flow.StateFlow<Int> = _ttsCurrentChapterIndex
    
    private val _utteranceId = kotlinx.coroutines.flow.MutableStateFlow("")
    override val utteranceId: kotlinx.coroutines.flow.StateFlow<String> = _utteranceId
    
    override var meta by mutableStateOf<MediaMetadataCompat?>(null)
    
    private val _isLoading = kotlinx.coroutines.flow.MutableStateFlow(false)
    override val isLoading: kotlinx.coroutines.flow.StateFlow<Boolean> = _isLoading
    
    private val _uiChapters = kotlinx.coroutines.flow.MutableStateFlow<List<Chapter>>(emptyList())
    override val uiChapters: kotlinx.coroutines.flow.StateFlow<List<Chapter>> = _uiChapters
    
    private val _isDrawerAsc = kotlinx.coroutines.flow.MutableStateFlow(false)
    override val isDrawerAsc: kotlinx.coroutines.flow.StateFlow<Boolean> = _isDrawerAsc
    
    // Cache status for Gradio TTS paragraphs
    var cachedParagraphs by mutableStateOf<Set<Int>>(emptySet())
    var loadingParagraphs by mutableStateOf<Set<Int>>(emptySet())
    
    // Gradio TTS flag (for online TTS engines)
    var useGradioTTS by mutableStateOf(false)
    
    // Timestamp when TTS actually starts speaking current paragraph (for highlighter sync)
    private val _paragraphSpeakingStartTime = kotlinx.coroutines.flow.MutableStateFlow(0L)
    val paragraphSpeakingStartTime: kotlinx.coroutines.flow.StateFlow<Long> = _paragraphSpeakingStartTime
    
    fun setParagraphSpeakingStartTime(value: Long) { _paragraphSpeakingStartTime.value = value }
    
    // Alias for speechSpeed (for compatibility)
    val speechRate: kotlinx.coroutines.flow.StateFlow<Float> get() = _speechSpeed
    
    fun setSpeechRate(value: Float) { _speechSpeed.value = value }
    
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
            chapter.content.firstOrNull() is Text -> {
                chapter.content
                    .filterIsInstance<Text>()
                    .map { it.text }
                    .filter { it.isNotBlank() }
                    .map { it.trim() }
            }
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
