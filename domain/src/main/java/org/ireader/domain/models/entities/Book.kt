package org.ireader.domain.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.ireader.core.utils.Constants.BOOK_TABLE
import org.ireader.source.models.BookInfo

@Serializable
@Entity(tableName = BOOK_TABLE)
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceId: Long,
    val link: String,
    val title: String,
    val translator: String = "",
    val author: String = "",
    val description: String = "",
    val genres: List<String> = emptyList(),
    val status: Int = 0,
    val cover: String = "",
    val customCover: String = "",
    val favorite: Boolean = false,
    val rating: Int = 0,
    val lastUpdated: Long = 0,
    val lastRead: Long = 0,
    val dataAdded: Long = 0,
    val viewer: Int = 0,
    val flags: Int = 0,
) {

    companion object {
        fun Book.toBookInfo(sourceId: Long): BookInfo {
            return BookInfo(
                cover = this.cover,
                link = this.link,
                title = this.title,
                translator = this.translator,
                status = this.status,
                genres = this.genres,
                description = this.description,
                author = this.author,
                rating = this.rating,
                viewer = this.viewer,
            )
        }

        const val UNKNOWN = 0
        const val ONGOING = 1
        const val COMPLETED = 2
        const val LICENSED = 3
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

}

fun updateBook(newBook: Book, oldBook: Book): Book {
    return Book(
        id = oldBook.id,
        sourceId = oldBook.sourceId,
        customCover = oldBook.customCover,
        flags = oldBook.flags,
        link = oldBook.link,
        lastRead = oldBook.lastRead,
        dataAdded = oldBook.dataAdded,
        lastUpdated = System.currentTimeMillis(),
        favorite = oldBook.favorite,
        title = newBook.title.ifBlank { oldBook.title },
        translator = newBook.translator.ifBlank { oldBook.translator },
        status = if (newBook.status != 0) newBook.status else oldBook.status,
        genres = newBook.genres.ifEmpty { oldBook.genres },
        description = newBook.description.ifBlank { oldBook.description },
        author = newBook.author.ifBlank { oldBook.author },
        cover = newBook.cover.ifBlank { oldBook.cover },
        rating = if (newBook.rating != 0) newBook.rating else oldBook.rating,
        viewer = if (newBook.viewer != 0) newBook.viewer else oldBook.viewer
    )
}

fun BookInfo.toBook(sourceId: Long): Book {
    return Book(
        id = 0,
        sourceId = sourceId,
        customCover = this.cover,
        cover = this.cover,
        flags = 0,
        link = this.link,
        lastRead = 0,
        dataAdded = 0L,
        lastUpdated = 0L,
        favorite = false,
        title = this.title,
        translator = this.translator,
        status = this.status,
        genres = this.genres,
        description = this.description,
        author = this.author,
        rating = this.rating,
        viewer = this.viewer
    )
}

fun BookInfo.fromBookInfo(sourceId: Long): Book {
    return Book(
        id = 0,
        sourceId = sourceId,
        customCover = this.cover,
        cover = this.cover,
        flags = 0,
        link = this.link,
        lastRead = 0,
        dataAdded = 0L,
        lastUpdated = 0L,
        favorite = false,
        title = this.title,
        translator = this.translator,
        status = this.status,
        genres = this.genres,
        description = this.description,
        author = this.author,
        rating = this.rating,
        viewer = this.viewer
    )
}