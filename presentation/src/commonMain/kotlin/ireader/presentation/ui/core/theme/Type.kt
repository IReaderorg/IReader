package ireader.presentation.ui.core.theme

import ireader.domain.models.common.ColorModel
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
        backgroundColor = ColorModel.fromArgb(this.backgroundColor),
        onTextColor = ColorModel.fromArgb(this.onTextColor),
        isDefault = id < 0
    )
}
