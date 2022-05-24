package org.ireader.common_models.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = CATEGORY_TABLE)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val sort: Int = 0,
    val updateInterval: Int = 0,
    val flags: Int = 0,
) {

    private val isUncategorized get() = id == UNCATEGORIZED_ID
    private val isAll get() = id == ALL_ID

    val isSystemCategory get() = isUncategorized || isAll

    companion object {
        const val ALL_ID = -2L
        const val UNCATEGORIZED_ID = -1L
    }

}

@Entity(
    tableName = BOOK_CATEGORY_TABLE,
    primaryKeys = [
        "bookId",
        "categoryId"
    ],
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("bookId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("categoryId"),
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class BookCategory(
    val bookId: Long,
    val categoryId: Long,
)

fun Category.toBookCategory(bookId: List<Long>): List<BookCategory> {
    return bookId.map {
        BookCategory(
            categoryId = this.id,
            bookId = it
        )
    }
}
