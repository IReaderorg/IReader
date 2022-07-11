package org.ireader.common_models.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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
    val date: String,
    val downloaded: Boolean = false,
) {
    companion object {
        fun UpdateWithInfo.toUpdate(): Update {
            return Update(
                id = this.id,
                bookId = this.bookId,
                chapterId = this.id,
                date = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            )
        }
    }
}

@Entity(
    tableName = UPDATE_TABLE,
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("bookId"),
            onDelete = ForeignKey.CASCADE,
        )
    ],
)
data class Update(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chapterId: Long,
    val bookId: Long,
    val date: Long,
)
