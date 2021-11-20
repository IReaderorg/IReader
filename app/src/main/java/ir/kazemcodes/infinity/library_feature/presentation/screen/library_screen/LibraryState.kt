package ir.kazemcodes.infinity.library_feature.presentation.screen.library_screen

import ir.kazemcodes.infinity.explore_feature.data.model.Book

data class LibraryState (
    val isLoading : Boolean = false,
    val books : List<Book> = emptyList(),
    val error: String = ""
)