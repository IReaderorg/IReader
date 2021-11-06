package ir.kazemcodes.infinity.data.remote.source.model

class ChapterImpl : Chapter {

    override lateinit var url: String

    override lateinit var name: String

    override var date_upload: Long = 0

    override var chapter_number: Float = -1f

    override var translator : String? = null
}