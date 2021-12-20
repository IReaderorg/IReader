package ir.kazemcodes.infinity.presentation.reader

import androidx.compose.ui.text.font.FontFamily

sealed class ReaderEvent {
    data class ChangeBrightness(val brightness : Float) : ReaderEvent()
    data class ChangeFontSize(val fontEvent: FontEvent) : ReaderEvent()
    data class ChangeFont(val fontFamily: FontFamily) : ReaderEvent()
}

sealed class FontEvent {
    object Increase: FontEvent()
    object Decrease: FontEvent()
}

