package org.ireader.domain.models.source

import org.ireader.source.models.BookInfo


data class BooksPage(
    val books: List<BookInfo> = emptyList(),
    val hasNextPage: Boolean = false,
)

