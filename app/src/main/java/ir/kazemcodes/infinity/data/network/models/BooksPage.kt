package ir.kazemcodes.infinity.api_feature.data

import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.models.Chapter


data class BooksPage(val Books: List<Book>, val hasNextPage: Boolean)
data class ChaptersPage(val chapters: List<Chapter>, val hasNextPage: Boolean)
data class ChapterPage(val content: String, val hasNextPage: Boolean? = null)