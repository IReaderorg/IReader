package ir.kazemcodes.infinity.explore_feature.data.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize


@Parcelize
@JsonClass(generateAdapter = true)
data class Chapter(
    @Json(name = "url")
    var bookName: String,
    @Json(name = "url")
    var link: String,
    @Json(name = "name")
    var title: String,
    @Json(name = "dateUpload")
    var index: Int? = null
) : Parcelable {
    companion object {
        fun create(): Chapter {
            return Chapter(title = "" , link = "", bookName = "")
        }
    }

//    @IgnoredOnParcel
//    var encodedLink = URLEncoder.encode(link , StandardCharsets.UTF_8.toString())
//    var decodedLink = URLEncoder.encode(encodedLink , StandardCharsets.UTF_8.toString())

}
