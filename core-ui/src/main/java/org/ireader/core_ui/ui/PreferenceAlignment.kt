package org.ireader.core_ui.ui

enum class PreferenceAlignment {
    Right,
    Left,
    Center,
    Justify,
}

fun mapTextAlign(textAlign: PreferenceAlignment): androidx.compose.ui.text.style.TextAlign {
    return when (textAlign) {
        PreferenceAlignment.Center -> androidx.compose.ui.text.style.TextAlign.Center
        PreferenceAlignment.Right -> androidx.compose.ui.text.style.TextAlign.Right
        PreferenceAlignment.Left -> androidx.compose.ui.text.style.TextAlign.Left
        PreferenceAlignment.Justify -> androidx.compose.ui.text.style.TextAlign.Justify
    }
}
