package org.ireader.domain.models.update_service_models

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class Release(
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String,
    @Json(name = "tag_name")
    val tagName: String,
    @Json(name = "html_url")
    val htmlUrl: String,
    @Json(name = "created_at")
    val createdAt: String,
)