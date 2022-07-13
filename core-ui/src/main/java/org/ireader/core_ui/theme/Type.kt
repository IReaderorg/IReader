package org.ireader.core_ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import org.ireader.common_models.theme.ReaderTheme

fun getDefaultFont(): FontType {
    return FontType("Roboto", FontFamily.Default)
}

val readerThemes = mutableListOf<ReaderColors>(
    ReaderColors(
        id = -1,
        Color(0xff000000),
        Color(0xffffffff),
        true
    ),
    ReaderColors(
        -2, Color(0xffffffff), Color(0xff000000),
        true
    ),
    ReaderColors(
        -3, Color(0xff262626),
        Color(
            0xFFE9E9E9,
        ),
        true
    ),
    ReaderColors(
        -4, Color(0xFF405A61), Color(0xFFFFFFFF),
        true
    ),
    ReaderColors(
        -5, Color(248, 249, 250), Color(51, 51, 51),
        true
    ),
    ReaderColors(
        -6, Color(150, 173, 252), Color(0xff000000),
        true
    ),
    ReaderColors(
        -7, Color(219, 225, 241), Color(0xff000000),
        true
    ),
    ReaderColors(
        -8, Color(237, 221, 110), Color(0xff000000),
        true
    ),
    ReaderColors(
        -9, Color(168, 242, 154), Color(0xff000000),
        true
    ),
    ReaderColors(
        -10, Color(233, 214, 107), Color(0xff000000),
        true
    ),
    ReaderColors(
        -11, Color(237, 209, 176), Color(0xff000000),
        true
    ),
    ReaderColors(
        -12, Color(185, 135, 220), Color(0xff000000),
        true
    ),
    ReaderColors(
        -13, Color(224, 166, 170), Color(0xff000000),
        true
    ),
    ReaderColors(
        -14, Color(248, 253, 137), Color(0xff000000),
        true
    ),
)

data class ReaderColors(
    val id: Long,
    val backgroundColor: Color,
    val onTextColor: Color,
    val isDefault: Boolean = false
)
fun ReaderColors.ReaderTheme(): ReaderTheme {
    return org.ireader.common_models.theme.ReaderTheme(
        id = this.id,
        backgroundColor = this.backgroundColor.toArgb(),
        onTextColor = this.onTextColor.toArgb(),
        isDefault = this.isDefault
    )
}

fun ReaderTheme.ReaderColors(): ReaderColors {
    return org.ireader.core_ui.theme.ReaderColors(
        id = id,
        backgroundColor = Color(this.backgroundColor),
        onTextColor = Color(this.onTextColor),
        isDefault = this.isDefault
    )
}
