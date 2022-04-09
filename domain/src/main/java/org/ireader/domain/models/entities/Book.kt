package org.ireader.domain.models.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.ireader.core.utils.Constants.BOOK_TABLE
import tachiyomi.source.model.MangaInfo
import java.util.*

@Serializable
@Entity(tableName = BOOK_TABLE)
data class Book(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id", defaultValue = "0")
    val id: Long = 0,
    @ColumnInfo("tableId", defaultValue = "0")
    val tableId: Long = 0,
    @ColumnInfo("sourceId")
    val sourceId: Long,
    @ColumnInfo("link")
    val link: String,
    @ColumnInfo("title")
    val title: String,
    @ColumnInfo("author", defaultValue = "")
    val author: String = "",
    @ColumnInfo("description", defaultValue = "")
    val description: String = "",
    @ColumnInfo("genres", defaultValue = "[]")
    val genres: List<String> = emptyList(),
    @ColumnInfo("status", defaultValue = "0")
    val status: Int = 0,
    @ColumnInfo("cover", defaultValue = "")
    val cover: String = "",
    @ColumnInfo("customCover", defaultValue = "")
    val customCover: String = "",
    @ColumnInfo("favorite", defaultValue = "0")
    val favorite: Boolean = false,
    @ColumnInfo("lastUpdated", defaultValue = "0")
    val lastUpdated: Long = 0,
    @ColumnInfo("dataAdded", defaultValue = "0")
    val dataAdded: Long = 0,
    @ColumnInfo("viewer", defaultValue = "0")
    val viewer: Int = 0,
    @ColumnInfo("flags", defaultValue = "0")
    val flags: Int = 0,
) {

    companion object {
        fun Book.toBookInfo(sourceId: Long): MangaInfo {
            return MangaInfo(
                cover = this.cover,
                key = this.link,
                title = this.title,
                status = this.status,
                genres = this.genres,
                description = this.description,
                author = this.author,
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
        dataAdded = oldBook.dataAdded,
        lastUpdated = Calendar.getInstance().timeInMillis,
        favorite = oldBook.favorite,
        title = newBook.title.ifBlank { oldBook.title },
        status = if (newBook.status != 0) newBook.status else oldBook.status,
        genres = newBook.genres.ifEmpty { oldBook.genres },
        description = newBook.description.ifBlank { oldBook.description },
        author = newBook.author.ifBlank { oldBook.author },
        cover = newBook.cover.ifBlank { oldBook.cover },
        viewer = if (newBook.viewer != 0) newBook.viewer else oldBook.viewer,
        tableId = oldBook.tableId,
    )
}

fun MangaInfo.toBook(sourceId: Long, tableId: Long = 0, lastUpdated: Long = 0): Book {
    return Book(
        id = 0,
        sourceId = sourceId,
        customCover = this.cover,
        cover = this.cover,
        flags = 0,
        link = this.key,
        dataAdded = 0L,
        lastUpdated = lastUpdated,
        favorite = false,
        title = this.title,
        status = this.status,
        genres = this.genres,
        description = this.description,
        author = this.author,
        tableId = tableId
    )
}

fun MangaInfo.fromBookInfo(sourceId: Long): Book {
    return Book(
        id = 0,
        sourceId = sourceId,
        customCover = this.cover,
        cover = this.cover,
        flags = 0,
        link = this.key,
        dataAdded = 0L,
        lastUpdated = 0L,
        favorite = false,
        title = this.title,
        status = this.status,
        genres = this.genres,
        description = this.description,
        author = this.author,
    )
}

data class BookWithInfo(
    val id: Long = 0,
    val title: String,
    val lastRead: Long = 0,
    val lastUpdated: Long = 0,
    val unread: Boolean = false,
    val totalChapters: Int = 0,
    val dateUpload: Long = 0,
    val dateFetch: Long = 0,
    val dataAdded: Long = 0,
    val sourceId: Long,
    val totalDownload: Int,
    val isRead: Int,
    val link: String,
    val status: Int = 0,
    val cover: String = "",
    val customCover: String = "",
    val favorite: Boolean = false,
    val tableId: Long = 0,
    val author: String = "",
    val description: String = "",
    val genres: List<String> = emptyList(),
    val viewer: Int = 0,
    val flags: Int = 0,
)

fun BookWithInfo.toBook(): Book {
    return Book(
        id = this.id,
        sourceId = sourceId,
        customCover = this.cover,
        cover = this.cover,
        flags = 0,
        link = this.link,
        dataAdded = 0L,
        lastUpdated = 0L,
        favorite = false,
        title = this.title,
        status = this.status,
        genres = this.genres,
        description = this.description,
        author = this.author,
    )
}
