package ir.kazemcodes.infinity.core.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Chapter(
    var bookName: String? = null,
    var link: String,
    var title: String,
    var content: List<String> = emptyList(),
    var dateUploaded: String? = null,
    var haveBeenRead: Boolean = false,
    var lastRead: Boolean = false,
    var source: String,
    var inLibrary:Boolean=false,
    var id: Int? = null,
) : Parcelable {
    companion object {
        fun create(): Chapter {
            return Chapter(
                link = "", title = "", source = ""
            )
        }
    }

    fun toChapterEntity(): ChapterEntity {
        return ChapterEntity(
            bookName = bookName,
            link = link,
            title = title,
            content = content,
            dateUploaded = dateUploaded,
            lastRead = lastRead,
            haveBeenRead = haveBeenRead,
            source = source,
            chapterId = id,
            inLibrary = inLibrary,
        )
    }

    fun isChapterNotEmpty(): Boolean {
        return content.joinToString().length > 10
    }

}
