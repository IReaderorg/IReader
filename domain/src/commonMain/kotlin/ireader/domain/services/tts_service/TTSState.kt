package ireader.domain.services.tts_service

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.core.source.Source
import ireader.domain.preferences.models.prefs.IReaderVoice
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.ExperimentalTime

/**
 * Common TTS state interface for both Android and Desktop
 * 
 * Uses StateFlow instead of Compose State to maintain clean architecture.
 */
interface TTSState {
    val isPlaying: StateFlow<Boolean>
    val ttsContent: StateFlow<List<String>?>
    val translatedTTSContent: StateFlow<List<String>?>
    val currentReadingParagraph: StateFlow<Int>
    val previousReadingParagraph: StateFlow<Int>
    val autoNextChapter: StateFlow<Boolean>
    val currentVoice: StateFlow<IReaderVoice?>
    val prevVoice: StateFlow<IReaderVoice?>
    val currentLanguage: StateFlow<String>
    val utteranceId: StateFlow<String>
    val prevLanguage: StateFlow<String>
    val pitch: StateFlow<Float>
    val prevPitch: StateFlow<Float>
    val speechSpeed: StateFlow<Float>
    val prevSpeechSpeed: StateFlow<Float>
    val sleepTime: StateFlow<Long>
    val ttsBook: StateFlow<Book?>
    val ttsChapter: StateFlow<Chapter?>
    val ttsSource: StateFlow<Source?>
    val ttsCatalog: StateFlow<CatalogLocal?>
    val ttsChapters: StateFlow<List<Chapter>>
    val ttsCurrentChapterIndex: StateFlow<Int>
    val isServiceConnected: StateFlow<Boolean>
    val isLoading: StateFlow<Boolean>
    val uiChapters: StateFlow<List<Chapter>>
    val isDrawerAsc: StateFlow<Boolean>
    @OptIn(ExperimentalTime::class)
    val startTime: StateFlow<kotlin.time.Instant?>
    val sleepMode: StateFlow<Boolean>
    
    // Setters for updating state
    fun setPlaying(value: Boolean)
    fun setTtsContent(value: List<String>?)
    fun setTranslatedTTSContent(value: List<String>?)
    fun setCurrentReadingParagraph(value: Int)
    fun setPreviousReadingParagraph(value: Int)
    fun setAutoNextChapter(value: Boolean)
    fun setCurrentVoice(value: IReaderVoice?)
    fun setPrevVoice(value: IReaderVoice?)
    fun setCurrentLanguage(value: String)
    fun setUtteranceId(value: String)
    fun setPrevLanguage(value: String)
    fun setPitch(value: Float)
    fun setPrevPitch(value: Float)
    fun setSpeechSpeed(value: Float)
    fun setPrevSpeechSpeed(value: Float)
    fun setSleepTime(value: Long)
    fun setTtsBook(value: Book?)
    fun setTtsChapter(value: Chapter?)
    fun setTtsCatalog(value: CatalogLocal?)
    fun setTtsChapters(value: List<Chapter>)
    fun setTtsCurrentChapterIndex(value: Int)
    fun setServiceConnected(value: Boolean)
    fun setLoading(value: Boolean)
    fun setUiChapters(value: List<Chapter>)
    fun setDrawerAsc(value: Boolean)
    @OptIn(ExperimentalTime::class)
    fun setStartTime(value: kotlin.time.Instant?)
    fun setSleepMode(value: Boolean)
}
