package ir.kazemcodes.infinity.domain.model.book

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VolumeItemRemote(
    @Json(name = "chapterItems")
    val chapterItems: List<ChapterItemRemote>,
    @Json(name = "volumeId")
    val id: Int,
    @Json(name = "volumeName")
    val name: String
)
