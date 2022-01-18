package ir.kazemcodes.infinity.core.data.network.models

import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter


data class BooksPage(
    val books: List<Book> = emptyList(),
    val hasNextPage: Boolean = false,
    val response: String = "",
    val ajaxLoaded: Boolean = true,
    val errorMessage: String = "",
)

data class BookPage(
    val book: Book = Book.create(),
    val ajaxLoaded: Boolean = true,
    val response: String = "",
    val errorMessage: String = "",
)

data class ChaptersPage(
    val chapters: List<Chapter> = emptyList(),
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