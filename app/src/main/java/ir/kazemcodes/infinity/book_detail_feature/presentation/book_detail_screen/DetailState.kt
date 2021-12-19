package ir.kazemcodes.infinity.book_detail_feature.presentation.book_detail_screen

import ir.kazemcodes.infinity.explore_feature.data.model.Book

data class DetailState (
    val isLoading : Boolean = false,
    val book: Book = Book.create(),
    val error: String = "",
    val loaded: Boolean = false
)