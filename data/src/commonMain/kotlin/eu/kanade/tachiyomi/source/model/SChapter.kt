@file:Suppress("PropertyName")

package eu.kanade.tachiyomi.source.model

import kotlinx.serialization.json.JsonObject
import java.io.Serializable

/**
 * Minimal SChapter interface shim for tsundoku extension compatibility.
 */
interface SChapter : Serializable {
    var url: String
    var name: String
    var chapter_number: Float
    var scanlator: String?
    var date_upload: Long
    var locked: Boolean
    var read: Boolean
        get() = false
        set(_) {}
    var last_page_read: Int
        get() = 0
        set(_) {}
    var memo: JsonObject

    fun copyFrom(other: SChapter) {
        name = other.name
        url = other.url
        date_upload = other.date_upload
        chapter_number = other.chapter_number
        scanlator = other.scanlator
        locked = other.locked
    }

    companion object {
        fun create(): SChapter = SChapterImpl()
    }
}

/**
 * Default SChapter implementation.
 */
class SChapterImpl : SChapter {
    override var url: String = ""
    override var name: String = ""
    override var chapter_number: Float = -1f
    override var scanlator: String? = null
    override var date_upload: Long = 0L
    override var locked: Boolean = false
    override var memo: JsonObject = JsonObject(emptyMap())
}
