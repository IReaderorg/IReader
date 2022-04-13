package org.ireader.domain.models.entities

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import org.ireader.core.utils.Constants

@Keep
data class UpdateWithInfo(
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
    val downloaded: Boolean = false,
) {
    companion object {
        fun UpdateWithInfo.toUpdate(): Update {
            return Update(
                id = this.id,
                bookId = this.bookId,
                chapterId = this.id,
                date = Clock.System.now().toEpochMilliseconds(),
            )
        }

    }
}

@Entity(tableName = Constants.UPDATE_TABLE)
@Keep
data class Update(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chapterId: Long,
    val bookId: Long,
    val date: Long,
) {
    companion object {
//        fun toUpdates(book: Book, chapter: Chapter): Update {
//            return Update(
//                bookId = chapter.bookId,
//                read = chapter.read,
//                chapterId = chapter.id,
//                bookTitle = book.title,
//                chapterDateUpload = chapter.dateUpload,
//                chapterLink = chapter.link,
//                chapterTitle = chapter.title,
//                cover = book.cover,
//                date = Clock.System.now().toEpochMilliseconds(),
//                favorite = book.favorite,
//                number = chapter.number,
//                sourceId = book.sourceId,
//                downloaded = chapter.content.joinToString().isNotEmpty()
//            )
//        }

    }

}
