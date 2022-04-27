package org.ireader.library

import org.ireader.common_models.entities.Book


sealed class BookListItem(val name: String) {

    data class Item(val book: Book) : BookListItem(book.title)
}