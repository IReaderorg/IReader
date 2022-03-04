package org.ireader.domain.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.ireader.core.utils.Constants.CHAPTER_TABLE
import sources.model.ChapterInfo


@Serializable
@Entity(tableName = CHAPTER_TABLE)
data class Chapter(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val link: String,
    val title: String,
    val inLibrary: Boolean = false,
    val read: Boolean = false,
    val bookmark: Boolean = false,
    val progress: Int = 0,
    val dateUploaded: Long = 0,
    val dateFetch: Long = 0,
    val content: List<String> = emptyList(),
    var lastRead: Long = 0L,
    val number: Float = -1f,
    val translator: String = "",
) {

    val isRecognizedNumber get() = number >= 0
    fun isChapterNotEmpty(): Boolean {
        return content.joinToString().length > 10
    }


}

fun Chapter.toChapterInfo(): ChapterInfo {
    return ChapterInfo(
        key = this.link,
        scanlator = this.translator,
        name = this.title,
        dateUpload = this.dateUploaded,
        number = this.number,
    )
}


fun ChapterInfo.toChapter(bookId: Long): Chapter {
    return Chapter(
        title = name,
        link = key,
        bookId = bookId,
    )
}