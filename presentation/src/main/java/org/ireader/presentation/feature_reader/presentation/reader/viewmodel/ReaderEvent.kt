package org.ireader.presentation.feature_reader.presentation.reader.viewmodel

import android.content.Context
import androidx.annotation.Keep
import org.ireader.core.utils.Event


sealed class ReaderEvent : Event() {
    @Keep
    data class ChangeBrightness(val brightness: Float, val context: Context) : ReaderEvent()
    @Keep
    data class ChangeFontSize(val fontSizeEvent: FontSizeEvent) : ReaderEvent()
    @Keep
    data class ChangeFont(val index: Int) : ReaderEvent()
    @Keep
    data class ToggleReaderMode(val enable: Boolean? = null) : ReaderEvent()
    object RestoreOrientation : ReaderEvent()
}

sealed class FontSizeEvent {
    object Increase : FontSizeEvent()
    object Decrease : FontSizeEvent()
}

