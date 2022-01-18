package ir.kazemcodes.infinity.feature_library.presentation

import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.presentation.layouts.DisplayMode
import ir.kazemcodes.infinity.core.presentation.layouts.LayoutType
import ir.kazemcodes.infinity.feature_library.presentation.components.SortType

data class LibraryState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val searchedBook: List<Book> = emptyList(),
    val error: String = "",
    val layout: LayoutType = DisplayMode.GridLayout.layout,
    val inSearchMode: Boolean = false,
    val searchQuery: String = "",
    val sortType: SortType = SortType.LastRead,
    val isSortAcs: Boolean = false,
)

