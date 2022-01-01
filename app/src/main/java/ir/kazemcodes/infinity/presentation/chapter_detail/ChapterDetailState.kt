package ir.kazemcodes.infinity.presentation.chapter_detail

import ir.kazemcodes.infinity.domain.models.LastReadChapter
import ir.kazemcodes.infinity.domain.models.remote.Chapter

data class ChapterDetailState (
    val isLoading : Boolean = false,
    val chapters : List<Chapter> = emptyList(),
    val error: String = "",
    val chapterOrderType: OrderType = OrderType.Ascending,
    val listOfLastReadChapter: List<LastReadChapter> = emptyList()
)