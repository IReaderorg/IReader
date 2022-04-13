package org.ireader.presentation.feature_library.presentation.viewmodel

import androidx.annotation.Keep
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.FilterType

@Keep
sealed class LibraryEvents {
    @Keep
    data class ToggleSearchMode(val inSearchMode: Boolean) : LibraryEvents()

    @Keep
    data class OnLayoutTypeChange(val layoutType: DisplayMode) : LibraryEvents()
    @Keep
    data class UpdateSearchInput(val query: String) : LibraryEvents()
    @Keep
    data class SearchBook(val query: String) : LibraryEvents()
    @Keep
    data class EnableFilter(val filterType: FilterType) : LibraryEvents()
}
