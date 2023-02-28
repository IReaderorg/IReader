package ireader.presentation.core.theme

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ireader.i18n.LocalizeHelper
import ireader.presentation.ui.core.theme.*
import kotlinx.coroutines.CoroutineScope
import org.kodein.di.compose.rememberInstance



@OptIn(ExperimentalTextApi::class)
@Composable
fun AppTheme(
    scope: CoroutineScope,
    content: @Composable() () -> Unit,
) {
    val vm: AppThemeViewModel by rememberInstance()
    val (materialColors, customColors) = vm.getColors()
    val rippleTheme = vm.getRippleTheme()
    val systemUiController = rememberSystemUiController()
    val transparentStatusBar = LocalTransparentStatusBar.current.enabled
    val isCustomColorEnable = LocalCustomSystemColor.current.enabled
    val status = LocalCustomSystemColor.current.enabled
    val navigation = LocalCustomSystemColor.current.enabled
    val customStatusColor = LocalCustomSystemColor.current
    val ctx = LocalContext.current
    val mainLocalizeHelper by rememberInstance<LocalizeHelper>()

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
            LocalLocalizeHelper provides mainLocalizeHelper,
            LocalGlobalCoroutineScope provides scope,
            content = content
        )
    }
}
