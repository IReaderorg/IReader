package ir.kazemcodes.infinity.feature_reader.presentation.reader

import android.content.Context
import ir.kazemcodes.infinity.core.domain.models.FontType
import ir.kazemcodes.infinity.core.utils.Event

sealed class ReaderEvent : Event() {
    data class ChangeBrightness(val brightness: Float,val context: Context) : ReaderEvent()
    data class ChangeFontSize(val fontSizeEvent: FontSizeEvent) : ReaderEvent()
    data class ChangeFont(val fontType: FontType) : ReaderEvent()
    data class ToggleReaderMode(val enable: Boolean? = null) : ReaderEvent()
    object RestoreOrientation : ReaderEvent()
}

sealed class FontSizeEvent {
    object Increase : FontSizeEvent()
    object Decrease : FontSizeEvent()
}

