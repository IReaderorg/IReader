@file:Suppress("PropertyName")

package eu.kanade.tachiyomi.source.model

import kotlinx.serialization.json.JsonObject

class SChapterImpl : SChapter {

    override lateinit var url: String

    override lateinit var name: String

    override var chapter_number: Float = -1f

    override var scanlator: String? = null

    override var date_upload: Long = 0

    override var locked: Boolean = false

    override var read: Boolean = false

    override var last_page_read: Int = 0

    override var memo: JsonObject = JsonObject(emptyMap())
}
