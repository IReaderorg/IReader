package ir.kazemcodes.infinity.api_feature.data

import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.models.Chapter


data class BooksPage(val books: List<Book> = emptyList(), val hasNextPage: Boolean = false)
data class ChaptersPage(val chapters: List<Chapter> = emptyList(), val hasNextPage: Boolean = false)
data class ChapterPage(val content: String, val hasNextPage: Boolean? = null)