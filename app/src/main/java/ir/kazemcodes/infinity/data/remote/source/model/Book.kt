package ir.kazemcodes.infinity.data.remote.source.model

interface Book {

    var url: String

    var title: String

    var author: String?

    var translator: String?

    var description: String?

    var genre: String?

    var status: Int

    var thumbnailUrl: String?

    var initialized: Boolean

    fun copyFrom(other: Book) {
        if (other.author != null) {
            author = other.author
        }

        if (other.translator != null) {
            translator = other.translator
        }

        if (other.description != null) {
            description = other.description
        }

        if (other.genre != null) {
            genre = other.genre
        }

        if (other.thumbnailUrl != null) {
            thumbnailUrl = other.thumbnailUrl
        }

        status = other.status

        if (!initialized) {
            initialized = other.initialized
        }
    }

    companion object {
        const val UNKNOWN = 0
        const val ONGOING = 1
        const val COMPLETED = 2
        const val LICENSED = 3

        fun create(): Book {
            return BookImp()
        }
    }
}