

package org.ireader.common_models.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.ireader.core_api.source.model.ChapterInfo
import org.ireader.core_api.source.model.Page

@Serializable
@Entity(
    tableName = CHAPTER_TABLE,
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("bookId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Chapter(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val key: String,
    val name: String,
    val read: Boolean = false,
    val bookmark: Boolean = false,
    val dateUpload: Long = 0,
    val dateFetch: Long = 0,
    val sourceOrder: Int = 0,
    val content: List<Page> = emptyList(),
    val number: Float = -1f,
    val translator: String = "",
) {

    val isRecognizedNumber get() = number >= 0
    fun isEmpty(): Boolean {
        return content.joinToString().isBlank()
    }
}

fun Chapter.toChapterInfo(): ChapterInfo {
    return ChapterInfo(
        key = this.key,
        scanlator = this.translator,
        name = this.name,
        dateUpload = this.dateUpload,
        number = this.number,
    )
}

fun ChapterInfo.toChapter(bookId: Long): Chapter {
    return Chapter(
        name = name,
        key = key,
        bookId = bookId,
        number = number,
        dateUpload = dateUpload,
        translator = this.scanlator,
    )
}
