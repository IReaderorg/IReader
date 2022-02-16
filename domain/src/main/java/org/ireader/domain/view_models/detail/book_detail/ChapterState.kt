package org.ireader.domain.view_models.detail.book_detail

import org.ireader.core.utils.UiText
import org.ireader.domain.models.entities.Chapter

data class ChapterState(
    override val isLoading: Boolean = false,
    override val error: UiText = UiText.DynamicString(""),
    val chapters: List<Chapter> = emptyList(),
    val loaded: Boolean = false,
    val chapterLoadingProgress: Float = 0f,
) : BaseState


interface BaseState {
    val isLoading: Boolean
    val error: UiText
}