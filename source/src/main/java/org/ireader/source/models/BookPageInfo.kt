package org.ireader.source.models


data class BookPageInfo(
    val mangases: List<BookInfo> = emptyList(),
    val hasNextPage: Boolean = false,
)

