package ireader.domain.services.tts_service

import android.speech.tts.Voice
import ireader.domain.preferences.models.prefs.IReaderVoice

// @Serializable
// data class IReaderVoice(
//    val name: String,
//    val language: String,
//    val country:String
// )

fun Voice.toIReaderVoice(): IReaderVoice {
    return IReaderVoice(
        name = this.name,
        language = this.locale.language,
        country = this.locale.country,
        localDisplayName = this.locale.displayName
    )
}

fun Voice.isSame(iReaderVoice: IReaderVoice?): Boolean {
    return this.name == iReaderVoice?.name && this.locale.country == iReaderVoice?.country && this.locale.language == iReaderVoice?.language
}

// Extension properties for AndroidTTSState to provide var-style access to StateFlow properties
var AndroidTTSState.ttsChapter: ireader.domain.models.entities.Chapter?
    get() = ttsChapter.value
    set(value) = setTtsChapter(value)

var AndroidTTSState.ttsBook: ireader.domain.models.entities.Book?
    get() = ttsBook.value
    set(value) = setTtsBook(value)

var AndroidTTSState.ttsSource: ireader.core.source.Source?
    get() = ttsSource.value
    set(value) {
        // ttsSource is derived from ttsCatalog, so we don't set it directly
    }

var AndroidTTSState.ttsCatalog: ireader.domain.models.entities.CatalogLocal?
    get() = ttsCatalog.value
    set(value) = setTtsCatalog(value)

var AndroidTTSState.ttsChapters: List<ireader.domain.models.entities.Chapter>
    get() = ttsChapters.value
    set(value) = setTtsChapters(value)

var AndroidTTSState.currentReadingParagraph: Int
    get() = currentReadingParagraph.value
    set(value) = setCurrentReadingParagraph(value)

var AndroidTTSState.previousReadingParagraph: Int
    get() = previousReadingParagraph.value
    set(value) = setPreviousReadingParagraph(value)

var AndroidTTSState.isDrawerAsc: Boolean
    get() = isDrawerAsc.value
    set(value) = setDrawerAsc(value)

var AndroidTTSState.isPlaying: Boolean
    get() = isPlaying.value
    set(value) = setPlaying(value)

var AndroidTTSState.isServiceConnected: Boolean
    get() = isServiceConnected.value
    set(value) = setServiceConnected(value)

var AndroidTTSState.currentVoice: IReaderVoice?
    get() = currentVoice.value
    set(value) = setCurrentVoice(value)

var AndroidTTSState.currentLanguage: String
    get() = currentLanguage.value
    set(value) = setCurrentLanguage(value)

var AndroidTTSState.speechSpeed: Float
    get() = speechSpeed.value
    set(value) = setSpeechSpeed(value)

var AndroidTTSState.pitch: Float
    get() = pitch.value
    set(value) = setPitch(value)

var AndroidTTSState.autoNextChapter: Boolean
    get() = autoNextChapter.value
    set(value) = setAutoNextChapter(value)
