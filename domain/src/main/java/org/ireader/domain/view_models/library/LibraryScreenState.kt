package org.ireader.domain.view_models.library

import org.ireader.core.utils.UiText
import org.ireader.domain.R
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.FilterType
import org.ireader.domain.models.LayoutType
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book

data class LibraryScreenState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val searchedBook: List<Book> = emptyList(),
    val error: UiText = UiText.StringResource(R.string.no_error),
    val layout: LayoutType = DisplayMode.GridLayout.layout,
    val inSearchMode: Boolean = false,
    val searchQuery: String = "",
    val sortType: SortType = SortType.LastRead,
    val isSortAcs: Boolean = false,
    val unreadFilter: FilterType = FilterType.Disable,
    val currentScrollState: Int = 0,
)

