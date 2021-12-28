package ir.kazemcodes.infinity.presentation.book_detail

import ir.kazemcodes.infinity.domain.models.remote.Book

data class DetailState(
    val isLoading: Boolean = false,
    val book: Book = Book.create(),
    val error: String = "",
    val loaded: Boolean = false,
    val inLibrary: Boolean = false,

    )