package org.ireader.domain.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.ireader.core.utils.Constants.CHAPTER_TABLE


@Serializable
@Entity(tableName = CHAPTER_TABLE)
data class Chapter(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long = 0,
    val link: String,
    val title: String,
    val inLibrary: Boolean = false,
    val read: Boolean = false,
    val bookmark: Boolean = false,
    val progress: Int = 0,
    val dateUploaded: Long = 0,
    val dateFetch: Long = 0,
    val content: List<String> = emptyList(),
    val lastRead: Boolean = false,
    val number: Float = -1f,
    val translator: String = "",
) {

    val isRecognizedNumber get() = number >= 0
    fun isChapterNotEmpty(): Boolean {
        return content.joinToString().length > 10
    }
}