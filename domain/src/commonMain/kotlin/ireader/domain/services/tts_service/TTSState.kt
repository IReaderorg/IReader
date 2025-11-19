package ireader.domain.services.tts_service

import androidx.compose.runtime.State
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.core.source.Source
import ireader.domain.preferences.models.prefs.IReaderVoice
import kotlin.time.ExperimentalTime

/**
 * Common TTS state interface for both Android and Desktop
 */
interface TTSState {
    var isPlaying: Boolean
    var ttsContent: State<List<String>?>?
    var currentReadingParagraph: Int
    var previousReadingParagraph: Int
    var autoNextChapter: Boolean
    var currentVoice: IReaderVoice?
    var prevVoice: IReaderVoice?
    var currentLanguage: String
    var utteranceId: String
    var prevLanguage: String
    var pitch: Float
    var prevPitch: Float
    var speechSpeed: Float
    var prevSpeechSpeed: Float
    var sleepTime: Long
    var ttsBook: Book?
    var ttsChapter: Chapter?
    val ttsSource: Source?
    var ttsCatalog: CatalogLocal?
    var ttsChapters: List<Chapter>
    var ttsCurrentChapterIndex: Int
    var isServiceConnected: Boolean
    val isLoading: State<Boolean>
    val uiChapters: State<List<Chapter>>
    var isDrawerAsc: Boolean
    @OptIn(ExperimentalTime::class)
    var startTime: kotlin.time.Instant?
    var sleepMode: Boolean
}
