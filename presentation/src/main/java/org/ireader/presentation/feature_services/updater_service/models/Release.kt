package org.ireader.presentation.feature_services.updater_service.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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