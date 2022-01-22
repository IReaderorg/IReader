package ir.kazemcodes.infinity.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.kazemcodes.infinity.core.utils.Constants.CHAPTER_TABLE
import kotlinx.serialization.Serializable


@Serializable
@Entity(tableName = CHAPTER_TABLE)
data class Chapter(
    var bookName: String? = null,
    var link: String,
    var title: String,
    var dateUploaded: String? = null,
    var content: List<String> = emptyList(),
    var haveBeenRead: Boolean = false,
    var lastRead: Boolean = false,
    var source: String,
    var inLibrary:Boolean=false,
    @PrimaryKey val chapterId: Int? = null,
) {

    companion object {
        fun create(): Chapter {
            return Chapter(
                link = "", title = "", source = ""
            )
        }
    }


    fun isChapterNotEmpty(): Boolean {
        return content.joinToString().length > 10
    }
}