package ir.kazemcodes.infinity.presentation.screen.browse_screen

import ir.kazemcodes.infinity.data.remote.source.model.Book

data class BrowseScreenState(
    val isLoading : Boolean = false,
    val books: List<Book> = emptyList(),
    val error: String = ""
)
