package org.ireader.domain.view_models.explore

data class BrowseFilterState(
    val sortBy: String = "Default",
    val type: String = "Default",
    val status: String = "All",
    val genre: List<String> = emptyList(),
)