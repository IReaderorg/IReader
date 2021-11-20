package ir.kazemcodes.infinity.explore_feature.data.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ir.kazemcodes.infinity.library_feature.domain.model.BookEntity
import kotlinx.parcelize.Parcelize


@Parcelize
@JsonClass(generateAdapter = true)
data class Book(
    @Json(name = "url")
    var link: String,
    @Json(name = "title")
    var name: String,
    @Json(name = "thumbnail")
     var coverLink: String? = "",
    @Json(name = "author")
    var author: String?= "Unknown",
    @Json(name = "translator")
    var translator: String?= "Unknown",
    @Json(name = "description")
    var description: String? = "",
    @Json(name = "genre")
    var category: String? = "",
    @Json(name = "status")
    var status: Int = 0,
    @Json(name = "thumbnail")
    var initialized: Boolean = false,

    ) : Parcelable {

    companion object {
        const val UNKNOWN = 0
        const val ONGOING = 1
        const val COMPLETED = 2
        const val LICENSED = 3
        fun create(): Book {
            return Book(name = "" , link = "")
        }
    }
    fun toBookEntity() : BookEntity {
        return BookEntity(
            title = name,
            link = link,
            coverLink = coverLink,
            translator = translator,
            author = author,
            status = status,
            description = description,
            initialized = initialized,
            category = category
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