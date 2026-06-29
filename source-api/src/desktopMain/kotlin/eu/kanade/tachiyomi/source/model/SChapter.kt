@file:Suppress("PropertyName")

package eu.kanade.tachiyomi.source.model

import kotlinx.serialization.json.JsonObject
import java.io.Serializable

interface SChapter : Serializable {

    var url: String

    var name: String

    var chapter_number: Float

    var scanlator: String?

    var date_upload: Long

    var locked: Boolean

    /**
     * Local read state. Source implementations should NOT set this — it is populated
     * by the app only when delivering SChapter instances to [SourceTracker] callbacks.
     */
    var read: Boolean
        get() = false
        set(_) {}

    /**
     * Local last-page-read position. Source implementations should NOT set this — it is
     * populated by the app only when delivering SChapter instances to [SourceTracker] callbacks.
     */
    var last_page_read: Int
        get() = 0
        set(_) {}

    /**
     * Extra metadata associated with the chapter.
     *
     * The JSON object is not visible to users and intended for internal or source-specific
     * purposes. Apps may define their own namespaced keys (e.g., `"mihon.*"`) for sources to populate.
     *
     * This allows apps to attach and ask for custom information without affecting the visible
     * chapter data.
     *
     * @since tachiyomix 1.6
     */
    var memo: JsonObject

    fun copyFrom(other: SChapter) {
        name = other.name
        url = other.url
        date_upload = other.date_upload
        chapter_number = other.chapter_number
        scanlator = other.scanlator
        locked = other.locked
        memo = other.memo
    }

    companion object {
        fun create(): SChapter {
            return SChapterImpl()
        }
    }
}
