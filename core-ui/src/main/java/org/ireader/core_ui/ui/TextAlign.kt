package org.ireader.core_ui.ui

enum class TextAlign {
    Left,
    Center,
    Justify,
    Right
}

fun mapTextAlign(textAlign: TextAlign):androidx.compose.ui.text.style.TextAlign {
    return when(textAlign) {
        TextAlign.Center -> androidx.compose.ui.text.style.TextAlign.Center
        TextAlign.Right -> androidx.compose.ui.text.style.TextAlign.Right
        TextAlign.Left -> androidx.compose.ui.text.style.TextAlign.Left
        TextAlign.Justify -> androidx.compose.ui.text.style.TextAlign.Justify
    }
}
