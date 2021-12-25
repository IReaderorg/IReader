package ir.kazemcodes.infinity.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapter_entity")
data class ChapterEntity(
    val bookName: String? = null,
    val link: String,
    val title: String,
    var dateUploaded: String? = null,
    val content: String? = null,
    var haveBeenRead: Boolean = false,
    var lastRead: Boolean = false,
    val source: String,
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
            id = chapterId
        )
    }
}