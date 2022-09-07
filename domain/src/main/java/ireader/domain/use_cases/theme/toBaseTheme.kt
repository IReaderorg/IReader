package ireader.domain.use_cases.theme

import ireader.common.models.theme.BaseTheme
import ireader.common.models.theme.CustomTheme
import ireader.common.models.theme.toColorScheme
import ireader.common.models.theme.toCustomColorScheme
import ireader.common.models.theme.toCustomExtraColors
import ireader.common.models.theme.toExtraColor

fun CustomTheme.toBaseTheme(): BaseTheme {
    return BaseTheme(
        id = this.id,
        lightColor = this.lightColor.toColorScheme(),
        darkColor = this.darkColor.toColorScheme(),
        darkExtraColors = this.darkExtraColors.toExtraColor(),
        lightExtraColors = this.lightExtraColors.toExtraColor(),
        default = this.isDefault
    )
}

fun BaseTheme.toCustomTheme(): CustomTheme {
    return CustomTheme(
        id = this.id,
        lightColor = this.lightColor.toCustomColorScheme(),
        darkColor = this.darkColor.toCustomColorScheme(),
        lightExtraColors = this.lightExtraColors.toCustomExtraColors(),
        darkExtraColors = this.darkExtraColors.toCustomExtraColors(),
        isDefault = this.default
    )
}
