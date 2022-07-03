package org.ireader.domain.use_cases.theme

import org.ireader.common_models.theme.BaseTheme
import org.ireader.common_models.theme.CustomTheme
import org.ireader.common_models.theme.toColorScheme
import org.ireader.common_models.theme.toCustomColorScheme
import org.ireader.common_models.theme.toCustomExtraColors
import org.ireader.common_models.theme.toExtraColor

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
