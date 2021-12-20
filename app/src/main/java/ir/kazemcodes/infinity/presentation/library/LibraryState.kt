package ir.kazemcodes.infinity.presentation.library

import ir.kazemcodes.infinity.domain.models.Book

data class LibraryState (
    val isLoading : Boolean = false,
    val books : List<Book> = emptyList(),
    val error: String = ""
)