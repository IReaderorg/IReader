package org.ireader.infinity.presentation

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.LocalImageLoader
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
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

    AppColors(
        materialColors = materialColors,
        extraColors = customColors,
        typography = Typography,
        shape = Shapes
    ) {
        ProvideWindowInsets {
            CompositionLocalProvider(
                LocalRippleTheme provides rippleTheme,
                LocalImageLoader provides vm.coilLoader,
                content = content
            )
        }
    }

}