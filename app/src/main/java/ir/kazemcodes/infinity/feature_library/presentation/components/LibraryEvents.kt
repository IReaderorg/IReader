package ir.kazemcodes.infinity.feature_library.presentation.components

import ir.kazemcodes.infinity.core.presentation.layouts.DisplayMode

sealed class LibraryEvents {
    data class ToggleSearchMode(val inSearchMode: Boolean? = null) : LibraryEvents()
    data class UpdateLayoutType(val layoutType: DisplayMode) : LibraryEvents()
    data class UpdateSearchInput(val query: String) : LibraryEvents()
}
