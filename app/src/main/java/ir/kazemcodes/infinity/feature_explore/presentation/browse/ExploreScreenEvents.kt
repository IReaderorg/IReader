package ir.kazemcodes.infinity.feature_explore.presentation.browse

import ir.kazemcodes.infinity.core.presentation.layouts.DisplayMode

sealed class ExploreScreenEvents {
    data class OnLayoutTypeChnage(val layoutType: DisplayMode) : ExploreScreenEvents()
    data class ToggleMenuDropDown(val isShown: Boolean) : ExploreScreenEvents()
    data class ToggleSearchMode(val inSearchMode: Boolean) : ExploreScreenEvents()
    data class OnQueryChange(val query: String) : ExploreScreenEvents()
}
