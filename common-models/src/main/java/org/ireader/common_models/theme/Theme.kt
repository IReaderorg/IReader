package org.ireader.common_models.theme

import androidx.compose.material3.ColorScheme

data class Theme(
    val id: Long,
    val materialColors: ColorScheme,
    val extraColors: ExtraColors,
    val default:Boolean = false,
)

data class BaseTheme(
    val id: Long,
    val lightColor: ColorScheme,
    val darkColor: ColorScheme,
    val lightExtraColors: ExtraColors,
    val darkExtraColors: ExtraColors,
    val default: Boolean =false,
)
