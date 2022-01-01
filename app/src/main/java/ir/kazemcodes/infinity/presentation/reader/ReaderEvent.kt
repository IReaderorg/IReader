package ir.kazemcodes.infinity.presentation.reader

import ir.kazemcodes.infinity.domain.models.FontType
import ir.kazemcodes.infinity.domain.models.remote.Chapter

sealed class ReaderEvent {
    data class ChangeBrightness(val brightness: Float) : ReaderEvent()
    data class ChangeFontSize(val fontSizeEvent: FontSizeEvent) : ReaderEvent()
    data class ChangeFont(val fontType: FontType) : ReaderEvent()
    data class GetContent(val chapter: Chapter) : ReaderEvent()
    object GetContentLocally : ReaderEvent()
    object GetContentRemotely : ReaderEvent()
    data class ToggleReaderMode(val enable: Boolean? = null) : ReaderEvent()
}

sealed class FontSizeEvent {
    object Increase : FontSizeEvent()
    object Decrease : FontSizeEvent()
}

