package ireader.presentation.ui.settings.appearance

import ireader.domain.models.theme.CustomTheme
import ireader.domain.models.theme.Theme
import ireader.domain.models.theme.toColorScheme
import ireader.domain.models.theme.toCustomColorScheme
import ireader.domain.models.theme.toCustomExtraColors
import ireader.domain.models.theme.toExtraColor


fun CustomTheme.toBaseTheme(): Theme {
    return Theme(
        id = this.id,
        materialColors = this.materialColor.toColorScheme(this.dark),
        extraColors = this.extraColors.toExtraColor(),
        isDark = this.dark
    )
}

fun Theme.toCustomTheme(): CustomTheme {
    return CustomTheme(
        id = this.id,
        materialColor = this.materialColors.toCustomColorScheme(),
        extraColors = this.extraColors.toCustomExtraColors(),
        dark = this.isDark,

    )
}
