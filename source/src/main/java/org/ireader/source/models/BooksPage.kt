package org.ireader.source.models


data class BooksPage(
    val books: List<BookInfo> = emptyList(),
    val hasNextPage: Boolean = false,
)

