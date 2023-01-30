package ireader.presentation.core.theme

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.ExperimentalTextApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.ui.core.theme.AppTypography
import ireader.presentation.ui.core.theme.LocalCustomSystemCOlor
import ireader.presentation.ui.core.theme.LocalTransparentStatusBar
import ireader.presentation.ui.core.theme.Shapes
import ireader.presentation.ui.core.theme.isLight
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalTextApi::class)
@Composable
fun AppTheme(
    content: @Composable() () -> Unit,
) {
    val vm: AppThemeViewModel = getViewModel()
    val (materialColors, customColors) = vm.getColors()
    val rippleTheme = vm.getRippleTheme()
    val systemUiController = rememberSystemUiController()
    val transparentStatusBar = LocalTransparentStatusBar.current.enabled
    val isCustomColorEnable = LocalCustomSystemCOlor.current.enabled
    val status = LocalCustomSystemCOlor.current.enabled
    val navigation = LocalCustomSystemCOlor.current.enabled
    val customStatusColor = LocalCustomSystemCOlor.current

    systemUiController.setSystemBarsColor(
        color = customColors.bars,
        darkIcons = customColors.isBarLight,
        isNavigationBarContrastEnforced = false
    )
    DisposableEffect(
        customColors.isBarLight,
        transparentStatusBar,
        isCustomColorEnable,
        status,
        navigation
    ) {
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
        } else if (transparentStatusBar) {
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
        onDispose { }
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
