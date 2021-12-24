package ir.kazemcodes.infinity.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Chapter(
    var bookName: String? = null,
    var link: String,
    var title: String,
    var content: String? =null,
    var dateUploaded: String? = null,
    var haveBeenRead : Boolean = false,
    var lastRead:  Boolean = false,
    var source:  String,
) : Parcelable {
    companion object {
        fun create() : Chapter {
            return Chapter(
                link = "",title = "",source = ""
            )
        }
    }

    fun toChapterEntity() : ChapterEntity {
        return ChapterEntity(
            bookName = bookName,
            link = link,
            title = title,
            content = content,
            dateUploaded = dateUploaded,
            lastRead = lastRead,
            haveBeenRead = haveBeenRead,
            source = source
        )
    }

//    @IgnoredOnParcel
//    var encodedLink = URLEncoder.encode(link , StandardCharsets.UTF_8.toString())
//    var decodedLink = URLEncoder.encode(encodedLink , StandardCharsets.UTF_8.toString())

}
