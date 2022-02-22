package org.ireader.extensions.sources.en.mtlnovel

import kotlinx.serialization.Serializable

@Serializable
data class mtlSearchItem(
    val items: List<Item>,
)