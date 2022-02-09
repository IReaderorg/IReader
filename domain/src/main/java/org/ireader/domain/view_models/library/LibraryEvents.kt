package org.ireader.domain.view_models.library

import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.FilterType

sealed class LibraryEvents {
    data class ToggleSearchMode(val inSearchMode: Boolean) : LibraryEvents()
    data class OnLayoutTypeChange(val layoutType: DisplayMode) : LibraryEvents()
    data class UpdateSearchInput(val query: String) : LibraryEvents()
    data class SearchBook(val query: String) : LibraryEvents()
    data class EnableFilter(val filterType: FilterType) : LibraryEvents()
}
