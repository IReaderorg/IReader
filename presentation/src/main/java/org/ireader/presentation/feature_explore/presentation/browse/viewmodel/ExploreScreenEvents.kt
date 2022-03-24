package org.ireader.presentation.feature_explore.presentation.browse.viewmodel

import org.ireader.domain.models.DisplayMode

sealed class ExploreScreenEvents {
    data class OnLayoutTypeChnage(val layoutType: DisplayMode) : ExploreScreenEvents()
    data class ToggleSearchMode(val inSearchMode: Boolean) : ExploreScreenEvents()
    data class OnQueryChange(val query: String) : ExploreScreenEvents()
}
