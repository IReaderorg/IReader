package ireader.core.ui.ui

import androidx.compose.ui.Alignment

enum class PreferenceTextAlignment {
    Right,
    Left,
    Center,
    Justify,
    Hide,

}
enum class PreferenceAlignment {
    TopLeft,
    BottomLeft,
    Hide,

}

fun PreferenceAlignment.mapAlignment() : Alignment? {
    return when(this) {
        PreferenceAlignment.TopLeft -> Alignment.TopEnd
        PreferenceAlignment.BottomLeft -> Alignment.BottomEnd
        else -> null
    }
}


fun mapTextAlign(textAlign: PreferenceTextAlignment): androidx.compose.ui.text.style.TextAlign {
    return when (textAlign) {
        PreferenceTextAlignment.Center -> androidx.compose.ui.text.style.TextAlign.Center
        PreferenceTextAlignment.Right -> androidx.compose.ui.text.style.TextAlign.Right
        PreferenceTextAlignment.Left -> androidx.compose.ui.text.style.TextAlign.Left
        PreferenceTextAlignment.Justify -> androidx.compose.ui.text.style.TextAlign.Justify
        PreferenceTextAlignment.Hide -> androidx.compose.ui.text.style.TextAlign.Justify
    }
}
