package org.ireader.presentation.feature_reader.presentation.reader.viewmodel

import android.content.Context
import org.ireader.core.utils.Event

sealed class ReaderEvent : Event() {
    data class ChangeBrightness(val brightness: Float, val context: Context) : ReaderEvent()
    data class ChangeFontSize(val fontSizeEvent: FontSizeEvent) : ReaderEvent()
    data class ChangeFont(val index: Int) : ReaderEvent()
    data class ToggleReaderMode(val enable: Boolean? = null) : ReaderEvent()
    object RestoreOrientation : ReaderEvent()
}

sealed class FontSizeEvent {
    object Increase : FontSizeEvent()
    object Decrease : FontSizeEvent()
}

