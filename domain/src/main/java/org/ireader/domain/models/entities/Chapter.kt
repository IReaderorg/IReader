package org.ireader.domain.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.ireader.core.utils.Constants.CHAPTER_TABLE


@Serializable
@Entity(tableName = CHAPTER_TABLE)
data class Chapter(
    @PrimaryKey(autoGenerate = true) val chapterId: Int = 0,
    var bookName: String? = null,
    var bookId: Int,
    var link: String,
    var title: String,
    var dateUploaded: String? = null,
    var dateAdded: Long? = null,
    var content: List<String> = emptyList(),
    var haveBeenRead: Boolean = false,
    var lastRead: Boolean = false,
    var source: String,
    var inLibrary: Boolean = false,
    var bookmarked: Boolean = false,
    var downloaded: Boolean = false,
    var scrollPosition: Int = 0,
) {

    companion object {
        fun create(): Chapter {
            return Chapter(
                link = "", title = "", source = "", bookId = 0
            )
        }
    }


    fun isChapterNotEmpty(): Boolean {
        return content.joinToString().length > 10
    }
}