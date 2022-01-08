package ir.kazemcodes.infinity.data.network.models

import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter


data class BooksPage(
    val books: List<Book> = emptyList(),
    val hasNextPage: Boolean = false,
    val isCloudflareEnabled: Boolean = false, val response: String = "",
)

data class BookPage(
    val book: Book = Book.create(),
    val isCloudflareEnabled: Boolean = false,
    val ajaxLoaded: Boolean = true,
    val response: String = "",
)

data class ChaptersPage(
    val chapters: List<Chapter> = emptyList(),
    val hasNextPage: Boolean = false,
    val ajaxLoaded: Boolean = true,
    val progress: Float = 0f,
    val isCloudflareEnabled: Boolean = false,
)

data class ChapterPage(
    val content: List<String> = emptyList(),
    val ajaxLoaded: Boolean = true,
    val hasNextPage: Boolean? = null,
    val isCloudflareEnabled: Boolean = false,
)