package org.ireader.domain.services.tts_service

import android.speech.tts.Voice
import android.support.v4.media.MediaMetadataCompat
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter
import org.ireader.core_api.source.Source
import org.ireader.core_ui.theme.FontType
import org.ireader.domain.services.tts_service.media_player.TTSService
import java.util.Locale
import javax.inject.Inject

interface TTSState {

    var isPlaying: Boolean
    var ttsContent: State<List<String>?>?

    var currentReadingParagraph: Int
    var prevPar: Int
    var autoNextChapter: Boolean
    var languages: List<Locale>
    var voices: List<Voice>
    var currentVoice: String
    var prevVoice: String
    var currentLanguage: String
    var utteranceId: String
    var prevLanguage: String
    var pitch: Float
    var prevPitch: Float
    var speechSpeed: Float
    var prevSpeechSpeed: Float
    var ttsBook: Book?
    var ttsChapter: Chapter?
    var ttsSource: Source?
    var ttsChapters: List<Chapter>
    var ttsCurrentChapterIndex: Int
    var font: FontType
    var fontSize: Int
    var lineHeight: Int
    var isServiceConnected: Boolean


    var meta : MediaMetadataCompat?
    val isLoading : State<Boolean>
    val uiPage : State<Int>
    val uiChapters : State<List<Chapter>>
    var isDrawerAsc : Boolean

}

class TTSStateImpl @Inject constructor() : TTSState {
    override var font by mutableStateOf<FontType>(FontType.Poppins)
    override var lineHeight by mutableStateOf<Int>(25)
    override var isServiceConnected by mutableStateOf<Boolean>(false)

    override var fontSize by mutableStateOf<Int>(18)
    override var currentReadingParagraph: Int by mutableStateOf<Int>(0)
    override var prevPar: Int by mutableStateOf<Int>(0)
    override var languages by mutableStateOf<List<Locale>>(emptyList())
    override var voices by mutableStateOf<List<Voice>>(emptyList())

    override var currentVoice by mutableStateOf<String>("")
    override var prevVoice by mutableStateOf<String>("")
    override var currentLanguage by mutableStateOf<String>("")
    override var prevLanguage by mutableStateOf<String>("")
    override var isPlaying by mutableStateOf<Boolean>(false)
    override var ttsContent: State<List<String>?>? =
        derivedStateOf { ttsChapter?.content?.filter { it.isNotBlank() }?.map { it.trim() } }
    override var autoNextChapter by mutableStateOf<Boolean>(false)
    override var pitch by mutableStateOf<Float>(.8f)
    override var prevPitch by mutableStateOf<Float>(.8f)
    override var speechSpeed by mutableStateOf<Float>(.8f)
    override var prevSpeechSpeed by mutableStateOf<Float>(.8f)
    override var ttsBook by mutableStateOf<Book?>(null)
    override var ttsSource by mutableStateOf<Source?>(null)
    override var ttsChapter by mutableStateOf<Chapter?>(null)
    override var ttsChapters by mutableStateOf<List<Chapter>>(emptyList())
    override var ttsCurrentChapterIndex by mutableStateOf<Int>(-1)

    override var utteranceId by mutableStateOf<String>("")


    override var meta : MediaMetadataCompat? by mutableStateOf(null)
    override var isLoading : State<Boolean> = derivedStateOf { meta?.getLong(TTSService.IS_LOADING) == 1L }
    override var uiPage : State<Int> = derivedStateOf { meta?.getLong(TTSService.PROGRESS)?.toInt()?:0 }
    override var uiChapters : State<List<Chapter>> = derivedStateOf { if (!isDrawerAsc) ttsChapters else ttsChapters.reversed() }
    override var isDrawerAsc :Boolean by mutableStateOf(false)

}

interface TTSSUIState {
    var uiBook : Book?
    var uiChapter: Chapter?
    var uiChapters: List<Chapter>

}
