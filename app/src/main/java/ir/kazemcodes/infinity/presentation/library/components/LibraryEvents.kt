package ir.kazemcodes.infinity.presentation.library.components

import ir.kazemcodes.infinity.presentation.layouts.DisplayMode

sealed class LibraryEvents {
    data class ToggleSearchMode(val inSearchMode: Boolean? = null) : LibraryEvents()
    data class UpdateLayoutType(val layoutType: DisplayMode) : LibraryEvents()
    data class UpdateSearchInput(val query: String) : LibraryEvents()
    data class SearchBooks(val query: String) : LibraryEvents()
}
