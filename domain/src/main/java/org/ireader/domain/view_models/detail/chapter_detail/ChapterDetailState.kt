package org.ireader.domain.view_models.detail.chapter_detail

import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter

data class ChapterDetailState(
    val isLoading: Boolean = false,
    val chapters: List<Chapter> = emptyList(),
    val book: Book = Book(title = "", link = "", sourceId = 0L),
    val isAsc: Boolean = true,
    val error: String = "",
    val chapterOrderType: OrderType = OrderType.Ascending,
    val localChapters: List<Chapter> = emptyList(),
    val reverse: Boolean = false,
    val currentScrollPosition: Int = 0,
)