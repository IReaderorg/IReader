package ir.kazemcodes.infinity.presentation.browse

import ir.kazemcodes.infinity.data.network.models.Source

sealed class BrowseScreenEvents {
    data class GetBooks(val source: Source) : BrowseScreenEvents()
    data class UpdatePage(val page: Int) : BrowseScreenEvents()
    data class UpdateLayoutType(val layoutType: LayoutType) : BrowseScreenEvents()
    data class ToggleMenuDropDown(val isShown: Boolean) : BrowseScreenEvents()
    data class SearchBooks(val query: String) : BrowseScreenEvents()
    data class ToggleSearchMode(val inSearchMode: Boolean? = null) : BrowseScreenEvents()
    data class UpdateSearchInput(val query: String) : BrowseScreenEvents()
}
