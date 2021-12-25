package ir.kazemcodes.infinity.presentation.library.components

import ir.kazemcodes.infinity.presentation.browse.LayoutType

sealed class LibraryEvents {
    object GetLocalBooks : LibraryEvents()
    data class ToggleSearchMode(val inSearchMode: Boolean? = null) : LibraryEvents()
    data class UpdateLayoutType(val layoutType: LayoutType) : LibraryEvents()
    data class UpdateSearchInput(val query: String) : LibraryEvents()
    data class SearchBooks(val query: String) : LibraryEvents()
}
