package org.ireader.domain.services.tts_service

import android.speech.tts.Voice
import kotlinx.serialization.Serializable

@Serializable
data class IReaderVoice(
    val name: String,
    val language: String,
    val country:String
)

fun Voice.toIReaderVoice(): IReaderVoice {
    return IReaderVoice(
        name = this.name,
        language = this.locale.language,
        country = this.locale.country
    )
}

fun Voice.isSame(iReaderVoice: IReaderVoice?) : Boolean {
    return this.name == iReaderVoice?.name && this.locale.country == iReaderVoice?.country &&  this.locale.language == iReaderVoice?.language
}