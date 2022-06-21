package org.ireader.presentation.theme

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.ireader.core_ui.theme.AppColors
import org.ireader.core_ui.theme.LocalCustomStatusBar
import org.ireader.core_ui.theme.LocalTransparentStatusBar
import org.ireader.core_ui.theme.Shapes
import org.ireader.core_ui.theme.isLight
import org.ireader.core_ui.theme.themes.AppTypography

@Composable
fun AppTheme(
    content: @Composable() () -> Unit,
) {
    val vm: AppThemeViewModel = hiltViewModel()
    val (materialColors, customColors) = vm.getColors()
    val rippleTheme = vm.getRippleTheme()
    val systemUiController = rememberSystemUiController()
    val transparentStatusBar = LocalTransparentStatusBar.current.enabled
    val isCustomColorEnable  = LocalCustomStatusBar.current.enabled
    val status  = LocalCustomStatusBar.current.enabled
    val navigation  = LocalCustomStatusBar.current.enabled
    val customStatusColor  = LocalCustomStatusBar.current

    systemUiController.setSystemBarsColor(
        color = customColors.bars,
        darkIcons =  customColors.isBarLight,
        isNavigationBarContrastEnforced = false
    )
    LaunchedEffect(customColors.isBarLight, transparentStatusBar,isCustomColorEnable,status,navigation) {
        val isLight = materialColors.isLight()
        val darkIcons =
            if (transparentStatusBar) isLight else customColors.isBarLight

        if (isCustomColorEnable) {
            systemUiController.setStatusBarColor(
                color = customStatusColor.statusBar,
                darkIcons = customStatusColor.statusBar.luminance() > 0.5,
            )
            systemUiController.setNavigationBarColor(
                color = customStatusColor.navigationBar,
                darkIcons = customStatusColor.navigationBar.luminance() > 0.5,
            )
        } else
            if (transparentStatusBar) {
            systemUiController.setStatusBarColor(
                color = Color.Transparent,
                darkIcons = darkIcons,
            )
            systemUiController.setNavigationBarColor(
                color = customColors.bars,
                darkIcons = customColors.isBarLight,
            )
        } else {
            systemUiController.setSystemBarsColor(
                color = customColors.bars,
                darkIcons = customColors.isBarLight,
            )
        }


    }

    AppColors(
        materialColors = materialColors,
        extraColors = customColors,
        typography = AppTypography,
        shape = Shapes
    ) {
        CompositionLocalProvider(
            LocalRippleTheme provides rippleTheme,
            content = content
        )
    }
}
