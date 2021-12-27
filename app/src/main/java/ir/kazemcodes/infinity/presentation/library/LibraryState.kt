package ir.kazemcodes.infinity.presentation.library

import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.presentation.layouts.LayoutType

data class LibraryState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val searchedBook: List<Book> = emptyList(),
    val error: String = "",
    val layout: LayoutType = LayoutType.GridLayout,
    val inSearchMode: Boolean = false,
    val searchQuery: String = "",
)

