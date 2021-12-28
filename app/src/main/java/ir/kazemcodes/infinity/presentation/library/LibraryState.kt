package ir.kazemcodes.infinity.presentation.library

import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.presentation.layouts.DisplayMode
import ir.kazemcodes.infinity.presentation.layouts.LayoutType

data class LibraryState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val searchedBook: List<Book> = emptyList(),
    val error: String = "",
    val layout: LayoutType = DisplayMode.GridLayout.layout,
    val inSearchMode: Boolean = false,
    val searchQuery: String = "",
)

