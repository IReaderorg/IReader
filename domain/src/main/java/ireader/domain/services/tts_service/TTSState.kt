package ireader.domain.services.tts_service

import android.speech.tts.Voice
import android.support.v4.media.MediaMetadataCompat
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.datetime.Instant
import ireader.common.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.common.models.entities.Chapter
import ireader.core.source.Source
import ireader.core.source.model.Text
import ireader.domain.preferences.models.prefs.IReaderVoice
import ireader.domain.services.tts_service.media_player.TTSService
import org.koin.core.annotation.Factory
import java.util.Locale


interface TTSState {

    var isPlaying: Boolean
    var ttsContent: State<List<String>?>?

    var currentReadingParagraph: Int
    var prevPar: Int
    var autoNextChapter: Boolean
    var languages: List<Locale>
    var voices: List<Voice>
    val uiVoices: List<IReaderVoice>
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

    var meta: MediaMetadataCompat?
    val isLoading: State<Boolean>

    val uiChapters: State<List<Chapter>>
    var isDrawerAsc: Boolean
    var startTime: Instant?
    var sleepMode: Boolean
}
@Factory
class TTSStateImpl() : TTSState {


    override var isServiceConnected by mutableStateOf<Boolean>(false)


    override var currentReadingParagraph: Int by mutableStateOf<Int>(0)
    override var prevPar: Int by mutableStateOf<Int>(0)
    override var languages by mutableStateOf<List<Locale>>(emptyList())
    override var voices by mutableStateOf<List<Voice>>(emptyList())
    override val uiVoices by derivedStateOf { voices.map { it.toIReaderVoice() } }

    override var currentVoice by mutableStateOf<IReaderVoice?>(null)
    override var prevVoice by mutableStateOf<IReaderVoice?>(null)
    override var currentLanguage by mutableStateOf<String>("")
    override var prevLanguage by mutableStateOf<String>("")
    override var isPlaying by mutableStateOf<Boolean>(false)
    override var ttsContent: State<List<String>?>? =
        derivedStateOf { ttsChapter?.content?.filter { it is Text }?.map { (it as? Text)?.text }?.filter { it != null && it.isNotBlank() }?.mapNotNull { it?.trim() } }
    override var autoNextChapter by mutableStateOf<Boolean>(false)
    override var pitch by mutableStateOf<Float>(.8f)
    override var prevPitch by mutableStateOf<Float>(.8f)
    override var speechSpeed by mutableStateOf<Float>(.8f)
    override var sleepTime by mutableStateOf<Long>(0)
    override var startTime by mutableStateOf<Instant?>(null)
    override var sleepMode by mutableStateOf<Boolean>(false)
    override var prevSpeechSpeed by mutableStateOf<Float>(.8f)
    override var ttsBook by mutableStateOf<Book?>(null)
    override val ttsSource by derivedStateOf { ttsCatalog?.source }
    override var ttsCatalog: CatalogLocal? by mutableStateOf<CatalogLocal?>(null)
    override var ttsChapter by mutableStateOf<Chapter?>(null)
    override var ttsChapters by mutableStateOf<List<Chapter>>(emptyList())
    override var ttsCurrentChapterIndex by mutableStateOf<Int>(-1)

    override var utteranceId by mutableStateOf<String>("")

    override var meta: MediaMetadataCompat? by mutableStateOf(null)
    override var isLoading: State<Boolean> = derivedStateOf { meta?.getLong(TTSService.IS_LOADING) == 1L }

    override var uiChapters: State<List<Chapter>> = derivedStateOf { if (!isDrawerAsc) ttsChapters else ttsChapters.reversed() }
    override var isDrawerAsc: Boolean by mutableStateOf(false)
}
