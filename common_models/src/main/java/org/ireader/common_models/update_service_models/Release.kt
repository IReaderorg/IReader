package org.ireader.common_models.update_service_models

import kotlinx.serialization.Serializable


@Serializable
data class Release(
    val id: Int,
    val name: String,
    val tag_name: String,
    val html_url: String,
    val created_at: String,
)