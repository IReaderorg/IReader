package ir.kazemcodes.infinity.presentation.reader

import androidx.compose.ui.text.font.FontFamily
import ir.kazemcodes.infinity.domain.models.Chapter

sealed class ReaderEvent {
    data class ChangeBrightness(val brightness : Float) : ReaderEvent()
    data class ChangeFontSize(val fontSizeEvent: FontSizeEvent) : ReaderEvent()
    data class ChangeFont(val fontFamily: FontFamily) : ReaderEvent()
    data class GetContent(val chapter: Chapter) : ReaderEvent()
    object GetContentLocally : ReaderEvent()
    object GetContentRemotely : ReaderEvent()
}

sealed class FontSizeEvent {
    object Increase: FontSizeEvent()
    object Decrease: FontSizeEvent()
}

