package ir.kazemcodes.infinity.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.Constants.BOOK_TABLE
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = BOOK_TABLE)
data class BookEntity(
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
            inLibrary = true,
            status = status,
            source = source,
            rating = rating
        )
    }
}
