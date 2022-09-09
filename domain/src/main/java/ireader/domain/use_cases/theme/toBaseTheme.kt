package ireader.domain.use_cases.theme

import ireader.common.models.theme.*

fun CustomTheme.toBaseTheme(): Theme {
    return Theme(
        id = this.id,
        materialColors = this.materialColor.toColorScheme(),
        extraColors = this.extraColors.toExtraColor(),
        isDark = this.isDark
    )
}

fun Theme.toCustomTheme(): CustomTheme {
    return CustomTheme(
        id = this.id,
        materialColor = this.materialColors.toCustomColorScheme(),
        extraColors = this.extraColors.toCustomExtraColors(),
        isDark = this.isDark,

    )
}
