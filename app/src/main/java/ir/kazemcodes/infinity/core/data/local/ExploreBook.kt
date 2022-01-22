package ir.kazemcodes.infinity.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.utils.Constants
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = Constants.EXPLORE_BOOK_TABLE)
data class ExploreBook(
    @PrimaryKey(autoGenerate = false) val id: String,
    val link: String,
    val bookName: String,
    val coverLink: String? = null,
    val description: List<String> = emptyList(),
    val author: String? = null,
    val translator: String? = null,
    val category: List<String> = emptyList(),
    val status: Int = -1,
    val rating: Int = 0,
    val source: String? = null,
    val isExploreMode:Boolean = false,
    val lastUpdated:Long = 0
) {
    fun toBook(): Book {
        return Book(
            id =id,
            link = link,
            bookName = bookName,
            coverLink = coverLink,
            description = description,
            author = author,
            translator = translator,
            category = category,
            inLibrary = false,
            status = status,
            source = source,
            rating = rating
        )
    }
}
