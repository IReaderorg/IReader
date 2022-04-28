package org.ireader.reader.viewmodel


sealed class FontSizeEvent {
    object Increase : FontSizeEvent()
    object Decrease : FontSizeEvent()
}

