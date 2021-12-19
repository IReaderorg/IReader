package ir.kazemcodes.infinity.explore_feature.data.model

import android.os.Parcelable
import ir.kazemcodes.infinity.local_feature.domain.model.ChapterEntity
import kotlinx.parcelize.Parcelize


@Parcelize
data class Chapter(
    var bookName: String? = null,
    var link: String,
    var title: String,
    var content: String? =null,
    var index: Int? = null,
    var chapterId: Int? = null,
    var dateAdded: String? = null,
) : Parcelable {
    companion object {
        fun create() : Chapter {
            return Chapter(
                link = "",title = ""
            )
        }
    }

    fun toChapterEntity() : ChapterEntity {
        return ChapterEntity(
            bookName = bookName,
            index = index,
            link = link,
            title = title,
            content = content,
            dateAdded = dateAdded,
        )
    }

//    @IgnoredOnParcel
//    var encodedLink = URLEncoder.encode(link , StandardCharsets.UTF_8.toString())
//    var decodedLink = URLEncoder.encode(encodedLink , StandardCharsets.UTF_8.toString())

}
