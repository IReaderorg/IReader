package ir.kazemcodes.infinity.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.kazemcodes.infinity.core.utils.Constants.BOOK_TABLE
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = BOOK_TABLE)
data class Book(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var link: String,
    var bookName: String,
    var coverLink: String? = null,
    var description: List<String> = emptyList(),
    var author: String? = null,
    var translator: String? = null,
    var category: List<String> = emptyList(),
    var status: Int = -1,
    var rating: Int = 0,
    var source: String? = null,
    var isExploreMode:Boolean = false,
    var inLibrary: Boolean = false,
    var dataAdded: Long = 0,
    var download:Boolean =false,
    var lastRead:Long = 0,
    var totalChapters:Int = 0,
    var unread:Boolean = true,
    var lastUpdated:Long =-1
) {


    companion object {
        const val UNKNOWN = 0
        const val ONGOING = 1
        const val COMPLETED = 2
        const val LICENSED = 3
        fun create(): Book {
            return Book(bookName = "", link = "")
        }
    }

    fun getStatusByName(): String {
        return when (status) {
            0 -> "UNKNOWN"
            1 -> "ONGOING"
            2 -> "COMPLETED"
            3 -> "LICENSED"
            else -> "UNKNOWN"
        }
    }

    fun toBookEntity(): Book {
        return Book(
            bookName = bookName,
            link = link,
            coverLink = coverLink,
            translator = translator,
            author = author,
            status = status,
            description = description,
            category = category,
            source = source,
            id = id,
            rating = rating,
            inLibrary = inLibrary,
            dataAdded = dataAdded,
            lastRead = lastRead,
            download = download,
            totalChapters = totalChapters,
            unread = unread,
            lastUpdated = lastUpdated
        )
    }
//    fun toExploreBook(): ExploreBook {
//        return ExploreBook(
//            bookName = bookName,
//            link = link,
//            coverLink = coverLink,
//            translator = translator,
//            author = author,
//            status = status,
//            description = description,
//            category = category,
//            source = source,
//            rating = rating,
//            lastUpdated = lastUpdated,
//            id=0
//        )
//    }

}
