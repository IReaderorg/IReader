package org.ireader.extensions.sources.en.mtlnovel

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val query: String,
    val results: List<Result>,
)