package ir.kazemcodes.infinity.feature_library.presentation.components

import ir.kazemcodes.infinity.core.presentation.layouts.DisplayMode

sealed class LibraryEvents {
    data class ToggleSearchMode(val inSearchMode: Boolean) : LibraryEvents()
    data class OnLayoutTypeChange(val layoutType: DisplayMode) : LibraryEvents()
    data class UpdateSearchInput(val query: String) : LibraryEvents()
    data class SearchBook(val query: String) : LibraryEvents()
    data class EnableFilter(val filterType: FilterType) : LibraryEvents()
}
