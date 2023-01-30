package ireader.presentation.ui.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import ireader.domain.models.theme.ReaderTheme
import ireader.domain.preferences.models.ReaderColors


fun ReaderColors.ReaderTheme(): ReaderTheme {
    return ReaderTheme(
        id = this.id,
        backgroundColor = this.backgroundColor.toArgb(),
        onTextColor = this.onTextColor.toArgb(),
    )
}

fun ReaderTheme.ReaderColors(): ReaderColors {
    return ReaderColors(
        id = id,
        backgroundColor = Color(this.backgroundColor),
        onTextColor = Color(this.onTextColor),
        isDefault = id < 0
    )
}
