package ir.kazemcodes.infinity.feature_explore.presentation.browse

import ir.kazemcodes.infinity.core.presentation.layouts.DisplayMode

sealed class ExploreScreenEvents {
    data class UpdatePage(val page: Int) : ExploreScreenEvents()
    data class UpdateLayoutType(val layoutType: DisplayMode) : ExploreScreenEvents()
    data class ToggleMenuDropDown(val isShown: Boolean) : ExploreScreenEvents()
    data class ToggleSearchMode(val inSearchMode: Boolean? = null) : ExploreScreenEvents()
    data class UpdateSearchInput(val query: String) : ExploreScreenEvents()
}
