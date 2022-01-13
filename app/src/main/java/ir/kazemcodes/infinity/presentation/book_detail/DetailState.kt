package ir.kazemcodes.infinity.presentation.book_detail

import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.remote.Book

data class DetailState(
    val source: Source,
    val book: Book = Book.create(),
    val inLibrary : Boolean = false,
    val isLoading: Boolean = false,
    val error: String = "",
    val loaded: Boolean = false,
    )