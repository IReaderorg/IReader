package ir.kazemcodes.infinity.presentation.book_detail

import ir.kazemcodes.infinity.domain.models.remote.Chapter

data class ChapterState(
    val isLoading: Boolean = false,
    val chapters: List<Chapter> = emptyList(),
    val error: String = "",
    val loaded: Boolean = false,
    val lastChapter: Chapter? = null,
    val chapterLoadingProgress: Float = 0f,
)