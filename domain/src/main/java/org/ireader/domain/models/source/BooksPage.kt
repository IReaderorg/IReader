package org.ireader.domain.models.source


data class BooksPage(
    val books: List<org.ireader.domain.models.entities.Book> = emptyList(),
    val hasNextPage: Boolean = false,
)


data class ChaptersPage(
    var chapters: List<org.ireader.domain.models.entities.Chapter> = emptyList(),
    var hasNextPage: Boolean = false,
)

data class ContentPage(
    val content: List<String> = emptyList(),
)