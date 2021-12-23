package ir.kazemcodes.infinity.domain.local_feature.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.kazemcodes.infinity.domain.models.Chapter

@Entity(tableName = "chapter_entity")
data class ChapterEntity(
    val bookName : String? = null,
    val link : String,
    val title: String,
    var dateUploaded: String? = null,
    val content: String? = null,
    var haveBeenRead : Boolean? =null,
    @PrimaryKey  val chapterId : Int? = null
) {
    fun toChapter() : Chapter {
        return Chapter(
            bookName = bookName,
            link = link,
            title = title,
            dateUploaded = dateUploaded,
            content = content,
            haveBeenRead = haveBeenRead
        )
    }
}