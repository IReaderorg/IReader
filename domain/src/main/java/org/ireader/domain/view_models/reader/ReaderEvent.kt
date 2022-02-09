package org.ireader.domain.view_models.reader

import android.content.Context
import org.ireader.core.utils.Event
import org.ireader.core_ui.theme.FontType

sealed class ReaderEvent : Event() {
    data class ChangeBrightness(val brightness: Float, val context: Context) : ReaderEvent()
    data class ChangeFontSize(val fontSizeEvent: FontSizeEvent) : ReaderEvent()
    data class ChangeFont(val fontType: FontType) : ReaderEvent()
    data class ToggleReaderMode(val enable: Boolean? = null) : ReaderEvent()
    object RestoreOrientation : ReaderEvent()
}

sealed class FontSizeEvent {
    object Increase : FontSizeEvent()
    object Decrease : FontSizeEvent()
}

