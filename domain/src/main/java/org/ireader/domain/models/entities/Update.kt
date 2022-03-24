package org.ireader.domain.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ireader.core.utils.Constants
import org.ireader.core.utils.currentTimeToLong


@Entity(tableName = Constants.UPDATE_TABLE)
data class Update(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chapterId: Long,
    val bookId: Long,
    val sourceId: Long,
    val chapterLink: String,
    val bookTitle: String,
    val cover: String = "",
    val favorite: Boolean = false,
    val chapterDateUpload: Long = 0,
    val chapterTitle: String,
    val read: Boolean = false,
    val number: Float = -1f,
    val date: Long,
) {
    companion object {
        fun toUpdates(book: Book, chapter: Chapter): Update {
            return Update(
                bookId = chapter.bookId,
                read = chapter.read,
                chapterId = chapter.id,
                bookTitle = book.title,
                chapterDateUpload = chapter.dateUpload,
                chapterLink = chapter.link,
                chapterTitle = chapter.title,
                cover = book.cover,
                date = currentTimeToLong(),
                favorite = book.favorite,
                number = chapter.number,
                sourceId = book.sourceId
            )
        }

    }

}
