package ir.kazemcodes.infinity.presentation.chapter_detail

import ir.kazemcodes.infinity.domain.models.Chapter

sealed class
ChapterDetailEvent {
    object ToggleOrder : ChapterDetailEvent()
    data class UpdateChapters(val chapters: List<Chapter>) : ChapterDetailEvent()
}
