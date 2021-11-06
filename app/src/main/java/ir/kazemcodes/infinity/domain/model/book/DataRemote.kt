package ir.kazemcodes.infinity.domain.model.book

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DataRemote(
    @Json(name = "bookInfo")
    val bookInfo: BookInfoRemote,
    @Json(name = "volumeItems")
    val volumeItems: List<VolumeItemRemote>
)
