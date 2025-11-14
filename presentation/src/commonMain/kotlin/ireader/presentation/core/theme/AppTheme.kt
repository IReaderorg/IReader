package ireader.presentation.core.theme

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.ExperimentalTextApi
import ireader.i18n.LocalizeHelper
import ireader.presentation.ui.core.theme.*
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject

@OptIn(ExperimentalTextApi::class)
@Composable
fun AppTheme(
    scope: CoroutineScope,
    content: @Composable() () -> Unit,
) {
    val vm = koinInject<AppThemeViewModel>()
    val (materialColors, customColors) = vm.getColors()
    val typography = vm.getTypography()
    val systemUiController = LocalISystemUIController.current
    systemUiController?.InitController()
    val transparentStatusBar = LocalTransparentStatusBar.current?.enabled ?: false
    val isCustomColorEnable = LocalCustomSystemColor.current?.enabled ?: false
    val status = LocalCustomSystemColor.current?.enabled ?: false
    val navigation = LocalCustomSystemColor.current?.enabled ?: false
    val customStatusColor = LocalCustomSystemColor.current
    val mainLocalizeHelper = koinInject<LocalizeHelper>()

    systemUiController?.setSystemBarsColor(
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

        if (isCustomColorEnable && customStatusColor != null) {
            systemUiController?.setStatusBarColor(
                color = customStatusColor.statusBar,
                darkIcons = customStatusColor.statusBar.luminance() > 0.5,
            )
            systemUiController?.setNavigationBarColor(
                color = customStatusColor.navigationBar,
                darkIcons = customStatusColor.navigationBar.luminance() > 0.5,
                navigationBarContrastEnforced = true
            )
        } else if (transparentStatusBar) {
            systemUiController?.setStatusBarColor(
                color = Color.Transparent,
                darkIcons = darkIcons,
            )
            systemUiController?.setNavigationBarColor(
                color = customColors.bars,
                darkIcons = customColors.isBarLight,
                navigationBarContrastEnforced = true
            )
        } else {
            systemUiController?.setSystemBarsColor(
                color = customColors.bars,
                darkIcons = customColors.isBarLight,
                isNavigationBarContrastEnforced = true
            )
        }
        onDispose { }
    }

    AppColors(
        materialColors = materialColors,
        extraColors = customColors,
        typography = typography,
        shape = Shapes
    ) {
        CompositionLocalProvider(
            LocalLocalizeHelper provides mainLocalizeHelper,
            LocalGlobalCoroutineScope provides scope,
            content = content
        )
    }
}
