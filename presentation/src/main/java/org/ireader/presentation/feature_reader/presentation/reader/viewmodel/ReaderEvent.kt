package org.ireader.presentation.feature_reader.presentation.reader.viewmodel

import android.content.Context
import androidx.annotation.Keep
import org.ireader.core.utils.Event


sealed class FontSizeEvent {
    object Increase : FontSizeEvent()
    object Decrease : FontSizeEvent()
}

