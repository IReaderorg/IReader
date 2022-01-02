package ir.kazemcodes.infinity.data.network.models

import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter


data class BooksPage(val books: List<Book> = emptyList()
                     , val hasNextPage: Boolean = false,
                     val isCloudflareEnabled : Boolean = false, val response : String = "")
data class ChaptersPage(
    val chapters: List<Chapter> = emptyList(), val hasNextPage: Boolean = false,
    val progress: Float = 0f,
)

data class ChapterPage(val content: List<String> = emptyList(), val hasNextPage: Boolean? = null)