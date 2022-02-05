package ir.kazemcodes.infinity.feature_detail.presentation.book_detail

import ir.kazemcodes.infinity.core.domain.models.Chapter

data class ChapterState(
    val isLoading: Boolean = false,
    val chapters: List<Chapter> = emptyList(),
    val error: String = "",
    val loaded: Boolean = false,
    val chapterLoadingProgress: Float = 0f,
)