package org.ireader.domain.view_models.detail.chapter_detail

import org.ireader.domain.models.LastReadChapter
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.source.Source
import org.ireader.presentation.feature_detail.presentation.chapter_detail.OrderType

data class ChapterDetailState(
    val isLoading: Boolean = false,
    val chapters: List<Chapter> = emptyList(),
    val book: Book = Book.create(),
    val isAsc: Boolean = true,
    val error: String = "",
    val chapterOrderType: OrderType = OrderType.Ascending,
    val listOfLastReadChapter: List<LastReadChapter> = emptyList(),
    val localChapters: List<Chapter> = emptyList(),
    val source: Source,
    val reverse: Boolean = false,
    val currentScrollPosition: Int = 0,
)