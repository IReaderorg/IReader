package ir.kazemcodes.infinity.domain.model.book

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RawChapterLinksRemote(
    @Json(name = "code")
    val code: Int,
    @Json(name = "data")
    val `data`: DataRemote,
    @Json(name = "msg")
    val msg: String
)
