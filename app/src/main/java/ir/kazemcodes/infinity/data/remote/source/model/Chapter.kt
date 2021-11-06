package ir.kazemcodes.infinity.data.remote.source.model

import java.io.Serializable

interface Chapter : Serializable {

    var url: String

    var name: String

    var date_upload: Long

    var chapter_number: Float

    var translator: String?

    fun copyFrom(other: Chapter) {
        name = other.name
        url = other.url
        date_upload = other.date_upload
        chapter_number = other.chapter_number
        translator = other.translator
    }

    companion object {
        fun create(): Chapter {
            return ChapterImpl()
        }
    }
}