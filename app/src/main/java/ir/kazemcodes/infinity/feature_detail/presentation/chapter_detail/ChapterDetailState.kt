package ir.kazemcodes.infinity.feature_detail.presentation.chapter_detail

import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.models.LastReadChapter

data class ChapterDetailState(
    val isLoading: Boolean = false,
    val chapters: List<Chapter> = emptyList(),
    val book: Book = Book.create(),
    val isAsc : Boolean = true,
    val error: String = "",
    val chapterOrderType: OrderType = OrderType.Ascending,
    val listOfLastReadChapter: List<LastReadChapter> = emptyList(),
    val localChapters : List<Chapter> = emptyList(),
    val source:Source,
    val reverse : Boolean = false
)