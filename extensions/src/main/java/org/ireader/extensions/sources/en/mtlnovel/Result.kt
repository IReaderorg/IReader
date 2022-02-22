package org.ireader.extensions.sources.en.mtlnovel

import kotlinx.serialization.Serializable

@Serializable
data class Result(
    val cn: String,
    val permalink: String,
    val shortname: String,
    val thumbnail: String,
    val title: String,
)