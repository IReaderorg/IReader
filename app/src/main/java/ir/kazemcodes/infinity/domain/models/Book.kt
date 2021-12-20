package ir.kazemcodes.infinity.domain.models

import android.os.Parcelable
import ir.kazemcodes.infinity.domain.local_feature.domain.model.BookEntity
import kotlinx.parcelize.Parcelize


@Parcelize
data class Book(

    var link: String,

    var bookName: String,

     var coverLink: String? = "",

    var source: String? = null,

    var author: String?= "",

    var translator: String?= "",

    var description: String? = "",

    var category: String? = "",

    var status: Int = 0,

    var inLibrary: Boolean = false,
    ) : Parcelable {

    companion object {
        const val UNKNOWN = 0
        const val ONGOING = 1
        const val COMPLETED = 2
        const val LICENSED = 3
        fun create(): Book {
            return Book(bookName = "" , link = "")
        }
    }
    fun toBookEntity() : BookEntity {
        return BookEntity(
            bookName = bookName,
            link = link,
            coverLink = coverLink,
            translator = translator,
            author = author,
            status = status,
            description = description,
            inLibrary = inLibrary,
            category = category,
            source=source
        )
    }


}
//interface Book {
//
//    var url: String
//
//    var title: String
//
//    var author: String?
//
//    var translator: String?
//
//    var description: String?
//
//    var genre: String?
//
//    var status: Int
//
//    var thumbnail: Any?
//
//    var initialized: Boolean
//
//
//
//
//    fun copyFrom(other: Book)  {
//        if (other.author != null) {
//            author = other.author
//        }
//
//        if (other.translator != null) {
//            translator = other.translator
//        }
//
//        if (other.description != null) {
//            description = other.description
//        }
//
//        if (other.genre != null) {
//            genre = other.genre
//        }
//
//        if (other.thumbnail != null) {
//            thumbnail = other.thumbnail
//        }
//
//        status = other.status
//
//        if (!initialized) {
//            initialized = other.initialized
//        }
//    }
//
//
//
//    companion object {
//        const val UNKNOWN = 0
//        const val ONGOING = 1
//        const val COMPLETED = 2
//        const val LICENSED = 3
//
//        fun create(): Book {
//            return BookImp()
//        }
//    }
//}