package ir.kazemcodes.infinity.library_feature.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter

@Entity(tableName = "chapter_entity")
data class ChapterEntity(
    val bookName : String,
    val link : String,
    val title: String,
    val index: Int? = null,
    val content: String? = null,
    @PrimaryKey val BookID : Int? = null
) {
    fun toChapter() : Chapter {
        return Chapter(
            bookName = bookName,
            link = link,
            title = title,
            index = index,
        )
    }
}