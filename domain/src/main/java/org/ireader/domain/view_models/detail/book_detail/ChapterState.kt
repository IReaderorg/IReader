package org.ireader.domain.view_models.detail.book_detail

import org.ireader.core.utils.UiText
import org.ireader.domain.models.entities.Chapter

data class ChapterState(
    val isLocalLoading: Boolean = false,
    val isRemoteLoading: Boolean = false,
    val chapters: List<Chapter> = emptyList(),
    val error: UiText = UiText.DynamicString(""),
    val loaded: Boolean = false,
    val chapterLoadingProgress: Float = 0f,
)