package ir.kazemcodes.infinity.presentation.library

import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.presentation.browse.LayoutType

data class LibraryState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val searchedBook: List<Book> = emptyList(),
    val error: String = "",
    val layout: LayoutType = LayoutType.GridLayout,
    val inSearchMode: Boolean = false,
    val searchQuery: String = "",
)

sealed class DisplayMode(val title: String) {
    object CompactModel : DisplayMode("Compact Layout")
    object GridLayout : DisplayMode("Grid Layout")
}