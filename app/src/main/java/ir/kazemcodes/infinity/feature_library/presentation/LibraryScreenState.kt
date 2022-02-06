package ir.kazemcodes.infinity.feature_library.presentation

import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.presentation.layouts.DisplayMode
import ir.kazemcodes.infinity.core.presentation.layouts.LayoutType
import ir.kazemcodes.infinity.core.utils.UiText
import ir.kazemcodes.infinity.feature_library.presentation.components.FilterType
import ir.kazemcodes.infinity.feature_library.presentation.components.SortType

data class LibraryScreenState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val searchedBook: List<Book> = emptyList(),
    val error: UiText  = UiText.StringResource(R.string.no_error),
    val layout: LayoutType = DisplayMode.GridLayout.layout,
    val inSearchMode: Boolean = false,
    val searchQuery: String = "",
    val sortType: SortType = SortType.LastRead,
    val isSortAcs: Boolean = false,
    val unreadFilter:FilterType = FilterType.Disable,
    val currentScrollState : Int = 0
)

