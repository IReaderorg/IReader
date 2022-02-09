package org.ireader.domain.models.source

import org.ireader.domain.models.entities.Book


data class BooksPage(
    val books: List<org.ireader.domain.models.entities.Book> = emptyList(),
    val hasNextPage: Boolean = false,
    val response: String = "",
    val ajaxLoaded: Boolean = true,
    val errorMessage: String = "",
)

data class BookPage(
    val book: Book = org.ireader.domain.models.entities.Book.create(),
    val ajaxLoaded: Boolean = true,
    val response: String = "",
    val errorMessage: String = "",
)

data class ChaptersPage(
    val chapters: List<org.ireader.domain.models.entities.Chapter> = emptyList(),
    val hasNextPage: Boolean = false,
    val ajaxLoaded: Boolean = true,
    val progress: Float = 0f,
    val errorMessage: String = "",
)

data class ChapterPage(
    val content: List<String> = emptyList(),
    val ajaxLoaded: Boolean = true,
    val hasNextPage: Boolean? = null,
    val errorMessage: String = "",
)