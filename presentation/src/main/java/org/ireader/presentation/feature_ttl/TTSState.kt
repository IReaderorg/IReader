package org.ireader.presentation.feature_ttl

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.support.v4.media.session.MediaSessionCompat
import androidx.compose.runtime.*
import dagger.hilt.android.qualifiers.ApplicationContext
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.core_api.source.Source
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

interface TTSState {
    var ttsIsLoading: Boolean
    var isPlaying: Boolean
    var ttsContent: State<List<String>?>?

    var currentReadingParagraph: Int
    var prevPar: Int
    var voiceMode: Boolean
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


}

@Singleton
open class TTSStateImpl @Inject constructor(@ApplicationContext context: Context) : TTSState {

    //val mediaSession = MediaSessionCompat(context, "mediaPlayer", null, null)
    var mediaSession : MediaSessionCompat? = null
    override var ttsIsLoading by mutableStateOf<Boolean>(false)
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
    override var voiceMode by mutableStateOf<Boolean>(false)
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

    var tts : TextToSpeech? = null

    override var utteranceId by mutableStateOf<String>("")
}
