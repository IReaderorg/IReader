package ir.kazemcodes.infinity.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.Constants.BOOK_TABLE
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = BOOK_TABLE)
data class BookEntity(
    val link: String,
    val bookName: String,
    val coverLink: String? = null,
    val description: List<String> = emptyList(),
    val author: String? = null,
    val translator: String? = null,
    val category: List<String> = emptyList(),
    val inLibrary: Boolean = false,
    val status: Int = -1,
    val rating: Int = 0,
    val source: String? = null,
    @PrimaryKey val bookId: Int? = null,
) {


    fun toBook(): Book {
        return Book(
            bookId =bookId,
            link = link,
            bookName = bookName,
            coverLink = coverLink,
            description = description,
            author = author,
            translator = translator,
            category = category,
            inLibrary = inLibrary,
            status = status,
            source = source,
            rating = rating
        )
    }
}
