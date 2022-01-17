package ir.kazemcodes.infinity.feature_explore.presentation.browse

import ir.kazemcodes.infinity.core.presentation.layouts.DisplayMode

sealed class BrowseScreenEvents {
    data class UpdatePage(val page: Int) : BrowseScreenEvents()
    data class UpdateLayoutType(val layoutType: DisplayMode) : BrowseScreenEvents()
    data class ToggleMenuDropDown(val isShown: Boolean) : BrowseScreenEvents()
    data class ToggleSearchMode(val inSearchMode: Boolean? = null) : BrowseScreenEvents()
    data class UpdateSearchInput(val query: String) : BrowseScreenEvents()
}
