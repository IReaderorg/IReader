package ir.kazemcodes.infinity.feature_reader.presentation.reader

import android.content.Context
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.models.FontType

sealed class ReaderEvent {
    data class ChangeBrightness(val brightness: Float,val context: Context) : ReaderEvent()
    data class ChangeFontSize(val fontSizeEvent: FontSizeEvent) : ReaderEvent()
    data class ChangeFont(val fontType: FontType) : ReaderEvent()
    data class GetContent(val chapter: Chapter) : ReaderEvent()
    data class ToggleReaderMode(val enable: Boolean? = null) : ReaderEvent()
}

sealed class FontSizeEvent {
    object Increase : FontSizeEvent()
    object Decrease : FontSizeEvent()
}

