package org.ireader.presentation.feature_library.presentation

import org.ireader.domain.models.entities.Book

sealed class BookListItem(val name: String) {
    data class Item(val book: Book) : BookListItem(book.title)
}