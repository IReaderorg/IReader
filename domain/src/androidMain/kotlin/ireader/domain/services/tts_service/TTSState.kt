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
    override var isServiceConnected by mutableStateOf(false)
    override var currentReadingParagraph by mutableStateOf(0)
    override var previousReadingParagraph by mutableStateOf(0)
    override var languages by mutableStateOf<List<Locale>>(emptyList())
    override var voices by mutableStateOf<List<Voice>>(emptyList())
    override val uiVoices by derivedStateOf { voices.map { it.toIReaderVoice() } }
    override var currentVoice by mutableStateOf<IReaderVoice?>(null)
    override var prevVoice by mutableStateOf<IReaderVoice?>(null)
    override var currentLanguage by mutableStateOf("")
    override var prevLanguage by mutableStateOf("")
    override var isPlaying by mutableStateOf(false)
    override var ttsContent: State<List<String>?>? =
        derivedStateOf { ttsChapter?.content?.filter { it is Text }?.map { (it as? Text)?.text }?.filter { it != null && it.isNotBlank() }?.mapNotNull { it?.trim() } }
    override var autoNextChapter by mutableStateOf(false)
    
    // Translated content for TTS (set externally when translation is available)
    var translatedTTSContent by mutableStateOf<List<String>?>(null)
    override var pitch by mutableStateOf(0.8f)
    override var prevPitch by mutableStateOf(0.8f)
    override var speechSpeed by mutableStateOf(0.8f)
    override var sleepTime by mutableStateOf(0L)
    @OptIn(ExperimentalTime::class)
    override var startTime by mutableStateOf<kotlin.time.Instant?>(null)
    override var sleepMode by mutableStateOf(false)
    override var prevSpeechSpeed by mutableStateOf(0.8f)
    override var ttsBook by mutableStateOf<Book?>(null)
    override val ttsSource by derivedStateOf { ttsCatalog?.source }
    override var ttsCatalog by mutableStateOf<CatalogLocal?>(null)
    override var ttsChapter by mutableStateOf<Chapter?>(null)
    override var ttsChapters by mutableStateOf<List<Chapter>>(emptyList())
    override var ttsCurrentChapterIndex by mutableStateOf(-1)
    override var utteranceId by mutableStateOf("")
    override var meta by mutableStateOf<MediaMetadataCompat?>(null)
    override var isLoading: State<Boolean> = derivedStateOf { meta?.getLong(TTSService.IS_LOADING) == 1L }
    override var uiChapters: State<List<Chapter>> = derivedStateOf { if (!isDrawerAsc) ttsChapters else ttsChapters.reversed() }
    override var isDrawerAsc by mutableStateOf(false)
}
