package ir.kazemcodes.infinity.data.remote.source.model

class BookImp : Book {

    override lateinit var url: String

    override lateinit var title: String

    override var translator: String? = null

    override var author: String? = null

    override var description: String? = null

    override var genre: String? = null

    override var status: Int = 0

    override var thumbnailUrl: String? = null

    override var initialized: Boolean = false

}