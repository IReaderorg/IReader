package org.ireader.presentation.theme

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.ireader.core_ui.theme.AppColors
import org.ireader.core_ui.theme.LocalCustomSystemCOlor
import org.ireader.core_ui.theme.LocalTransparentStatusBar
import org.ireader.core_ui.theme.Shapes
import org.ireader.core_ui.theme.isLight
import org.ireader.core_ui.theme.themes.createSingleGoogleFontFamily
import org.ireader.core_ui.theme.themes.createTypography

@OptIn(ExperimentalTextApi::class)
@Composable
fun AppTheme(
    content: @Composable() () -> Unit,
) {
    val vm: AppThemeViewModel = hiltViewModel()
    val (materialColors, customColors) = vm.getColors()
    val rippleTheme = vm.getRippleTheme()
    val systemUiController = rememberSystemUiController()
    val transparentStatusBar = LocalTransparentStatusBar.current.enabled
    val isCustomColorEnable  = LocalCustomSystemCOlor.current.enabled
    val status  = LocalCustomSystemCOlor.current.enabled
    val navigation  = LocalCustomSystemCOlor.current.enabled
    val customStatusColor  = LocalCustomSystemCOlor.current

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
        typography = createTypography(createSingleGoogleFontFamily("Poppins", weights = listOf(
            FontWeight.Normal,
            FontWeight.Light,
            FontWeight.Black,
            FontWeight.Bold,
            FontWeight.SemiBold,
            FontWeight.W100,
            FontWeight.W200,
            FontWeight.W300,
            FontWeight.W400,
            FontWeight.W500,
            FontWeight.W600,
            FontWeight.W700,
            FontWeight.W800,
            FontWeight.W900,
        ))),
        shape = Shapes
    ) {
        CompositionLocalProvider(
            LocalRippleTheme provides rippleTheme,
            content = content
        )
    }
}
