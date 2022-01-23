package ir.kazemcodes.infinity.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.kazemcodes.infinity.core.utils.Constants.CHAPTER_TABLE
import kotlinx.serialization.Serializable


@Serializable
@Entity(tableName = CHAPTER_TABLE)
data class Chapter(
    @PrimaryKey(autoGenerate = true) val chapterId: Int = 0,
    var bookName: String? = null,
    var bookId : Int,
    var link: String,
    var title: String,
    var dateUploaded: String? = null,
    var content: List<String> = emptyList(),
    var haveBeenRead: Boolean = false,
    var lastRead: Boolean = false,
    var source: String,
    var inLibrary:Boolean=false,
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