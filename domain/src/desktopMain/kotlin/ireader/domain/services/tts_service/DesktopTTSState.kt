package ireader.domain.services.tts_service

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.source.Source
import ireader.core.source.model.Text
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.models.prefs.IReaderVoice
import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

/**
 * Desktop implementation of TTS state
 */
class DesktopTTSState : TTSState {
    override var isServiceConnected by mutableStateOf(false)
    override var currentReadingParagraph by mutableStateOf(0)
    override var previousReadingParagraph by mutableStateOf(0)
    override var currentVoice by mutableStateOf<IReaderVoice?>(null)
    override var prevVoice by mutableStateOf<IReaderVoice?>(null)
    override var currentLanguage by mutableStateOf("")
    override var prevLanguage by mutableStateOf("")
    override var isPlaying by mutableStateOf(false)
    override var ttsContent: State<List<String>?>? =
        derivedStateOf { 
            ttsChapter?.content
                ?.filter { it is Text }
                ?.map { (it as? Text)?.text }
                ?.filter { it != null && it.isNotBlank() }
                ?.mapNotNull { it?.trim() }
        }
    override var autoNextChapter by mutableStateOf(false)
    override var pitch by mutableStateOf(0.8f)
    override var prevPitch by mutableStateOf(0.8f)
    override var speechSpeed by mutableStateOf(0.8f)
    override var sleepTime by mutableStateOf(0L)
    @OptIn(ExperimentalTime::class)
    override var startTime by mutableStateOf<Instant?>(null)
    override var sleepMode by mutableStateOf(false)
    override var prevSpeechSpeed by mutableStateOf(0.8f)
    override var ttsBook by mutableStateOf<Book?>(null)
    override val ttsSource by derivedStateOf { ttsCatalog?.source }
    override var ttsCatalog by mutableStateOf<CatalogLocal?>(null)
    override var ttsChapter by mutableStateOf<Chapter?>(null)
    override var ttsChapters by mutableStateOf<List<Chapter>>(emptyList())
    override var ttsCurrentChapterIndex by mutableStateOf(-1)
    override var utteranceId by mutableStateOf("")
    override var isLoading: State<Boolean> = derivedStateOf { false }
    override var uiChapters: State<List<Chapter>> = derivedStateOf { 
        if (!isDrawerAsc) ttsChapters else ttsChapters.reversed() 
    }
    override var isDrawerAsc by mutableStateOf(false)
}
