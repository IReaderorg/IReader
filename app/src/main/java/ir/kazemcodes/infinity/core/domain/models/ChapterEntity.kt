package ir.kazemcodes.infinity.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.Constants.CHAPTER_TABLE
import kotlinx.serialization.Serializable


@Serializable
@Entity(tableName = CHAPTER_TABLE)
data class ChapterEntity(
    val bookName: String? = null,
    val link: String,
    val title: String,
    var dateUploaded: String? = null,
    val content: List<String> = emptyList(),
    var haveBeenRead: Boolean = false,
    var lastRead: Boolean = false,
    val source: String,
    var inLibrary:Boolean=false,
    @PrimaryKey val chapterId: Int? = null,
) {
    fun toChapter(): Chapter {
        return Chapter(
            bookName = bookName,
            link = link,
            title = title,
            dateUploaded = dateUploaded,
            content = content,
            haveBeenRead = haveBeenRead,
            lastRead = lastRead,
            source = source,
            id = chapterId,
            inLibrary = inLibrary
        )
    }
}