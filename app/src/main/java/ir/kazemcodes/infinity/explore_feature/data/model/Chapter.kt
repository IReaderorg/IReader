package ir.kazemcodes.infinity.explore_feature.data.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ir.kazemcodes.infinity.library_feature.domain.model.ChapterEntity
import kotlinx.parcelize.Parcelize


@Parcelize
@JsonClass(generateAdapter = true)
data class Chapter(
    @Json(name = "bookName")
    var bookName: String? = null,
    @Json(name = "link")
    var link: String,
    @Json(name = "title")
    var title: String,
    @Json(name = "content")
    var content: String? =null,
    @Json(name = "index")
    var index: Int? = null,
    @Json(name = "chapterId")
    var chapterId: Int? = null,
    @Json(name = "dataAdded")
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
