package ir.kazemcodes.infinity.explore_feature.presentation.screen.browse_screen

import ir.kazemcodes.infinity.explore_feature.data.model.Book

data class BrowseScreenState(
    val isLoading : Boolean = false,
    val books: List<Book> = emptyList(),
    val error: String = "",
    val page: Int = 1,
)
