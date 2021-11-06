package ir.kazemcodes.infinity.domain.model.book

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_table")
data class BookEntity(
    val id: String,
    val link: String,
    val name: String,
    val rating: Float,
    val author: String,
    val coverLink: String,
    val category: String,
    val description: String,
    @PrimaryKey val BookID : Int? = null
)
