package ireader.ui.settings.appearance

import ireader.common.models.theme.CustomTheme
import ireader.common.models.theme.Theme
import ireader.common.models.theme.toColorScheme
import ireader.common.models.theme.toCustomColorScheme
import ireader.common.models.theme.toCustomExtraColors
import ireader.common.models.theme.toExtraColor


fun CustomTheme.toBaseTheme(): Theme {
    return Theme(
        id = this.id,
        materialColors = this.materialColor.toColorScheme(),
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
