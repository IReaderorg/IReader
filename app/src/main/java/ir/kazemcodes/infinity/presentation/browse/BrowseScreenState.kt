package ir.kazemcodes.infinity.presentation.browse

import ir.kazemcodes.infinity.data.network.models.BooksPage
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.presentation.layouts.LayoutType

data class BrowseScreenState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val error: String = "",
    val page: Int = 1,
    val searchPage: Int = 1,
    val layout: LayoutType = LayoutType.GridLayout,
    val isMenuDropDownShown: Boolean = false,
    val isSearchModeEnable: Boolean = false,
    val searchQuery: String = "",
    val searchedBook: BooksPage = BooksPage(),
    val isLatestUpdateMode : Boolean = true
    )

