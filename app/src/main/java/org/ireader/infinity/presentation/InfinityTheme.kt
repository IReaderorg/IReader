package org.ireader.infinity.presentation

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.ireader.core.utils.DeviceUtil.isMiui
import org.ireader.core_ui.theme.AppColors
import org.ireader.core_ui.theme.LocalTransparentStatusBar
import org.ireader.core_ui.theme.Shapes
import org.ireader.core_ui.theme.Typography
import org.ireader.domain.view_models.AppThemeViewModel


@Composable
fun InfinityTheme(
    content: @Composable() () -> Unit,
) {
    val vm: AppThemeViewModel = hiltViewModel()
    val (materialColors, customColors) = vm.getColors()
    val rippleTheme = vm.getRippleTheme()
    val systemUiController = rememberSystemUiController()
    val transparentStatusBar = LocalTransparentStatusBar.current.enabled


    LaunchedEffect(customColors.isBarLight, transparentStatusBar) {
        //TODO miui is still not respond correctly to rememberSystemUiController
        if (isMiui) return@LaunchedEffect
        val darkIcons =
            if (transparentStatusBar) materialColors.isLight else customColors.isBarLight


        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = darkIcons,
            isNavigationBarContrastEnforced = false
        )

    }

    LaunchedEffect(customColors.isBarLight) {
        if (isMiui) return@LaunchedEffect
        val darkIcons =
            if (transparentStatusBar) materialColors.isLight else customColors.isBarLight
        systemUiController.setStatusBarColor(
            color = customColors.bars,
            darkIcons = darkIcons,
        )
        systemUiController.setNavigationBarColor(
            color = customColors.bars,
            darkIcons = darkIcons
        )

    }
    AppColors(
        materialColors = materialColors,
        extraColors = customColors,
        typography = Typography,
        shape = Shapes
    ) {
        ProvideWindowInsets {
            CompositionLocalProvider(
                LocalRippleTheme provides rippleTheme,
                content = content
            )
        }
    }

}