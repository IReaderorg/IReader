package org.ireader.presentation.feature_explore.presentation.browse.viewmodel

data class BrowseFilterState(
    val sortBy: String = "Default",
    val type: String = "Default",
    val status: String = "All",
    val genre: List<String> = emptyList(),
)