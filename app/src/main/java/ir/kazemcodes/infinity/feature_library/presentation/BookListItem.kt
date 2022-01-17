package ir.kazemcodes.infinity.feature_library.presentation

import ir.kazemcodes.infinity.core.domain.models.Book

sealed class BookListItem(val name: String) {
    data class Item(val book: Book) : BookListItem(book.bookName)
}