package ir.kazemcodes.infinity.library_feature.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.kazemcodes.infinity.explore_feature.data.model.Book

@Entity(tableName = "book_table")
data class BookEntity(
    val link: String,
    val title: String,
    val coverLink: String? = null,
    val description: String?,
    val author: String? = null,
    val translator: String? = null,
    val category: String? =null,
    val initialized: Boolean = false,
    val status : Int = -1,
    @PrimaryKey val BookID : Int? = null
) {

    fun toBook() : Book {
        return Book(
            link = link,
            name = title,
            coverLink = coverLink,
            description = description,
            author = author,
            translator = translator,
            category = category,
            initialized = initialized,
            status = status
        )
    }
}
