package org.ireader.presentation.feature_library.presentation

import androidx.annotation.Keep
import org.ireader.domain.models.entities.Book

@Keep
sealed class BookListItem(val name: String) {
    @Keep
    data class Item(val book: Book) : BookListItem(book.title)
}