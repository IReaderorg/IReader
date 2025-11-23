package ireader.presentation.core.theme

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.ExperimentalTextApi
import ireader.i18n.LocalizeHelper
import ireader.presentation.ui.core.theme.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
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
    val customStatusColor = LocalCustomSystemColor.current
    val isCustomColorEnable = customStatusColor?.enabled ?: false
    val mainLocalizeHelper = koinInject<LocalizeHelper>()
    
    val isLight = materialColors.isLight()
    
    // Snapshot the bar colors to ensure DisposableEffect detects changes
    val currentBarsColor = customColors.bars
    val currentIsBarLight = customColors.isBarLight

    // Handle system bars color with proper dependencies
    DisposableEffect(
        currentBarsColor,
        currentIsBarLight,
        transparentStatusBar,
        isCustomColorEnable,
        customStatusColor?.statusBar,
        customStatusColor?.navigationBar,
        isLight,
        materialColors  // Add materialColors as dependency to react to theme changes
    ) {
        // Determine status bar configuration
        val (statusBarColor, statusBarDarkIcons) = when {
            // Priority 1: Custom status bar color
            isCustomColorEnable && customStatusColor != null -> {
                customStatusColor.statusBar to (customStatusColor.statusBar.luminance() > 0.5)
            }
            // Priority 2: Transparent status bar
            transparentStatusBar -> {
                Color.Transparent to isLight
            }
            // Priority 3: Default bars color
            else -> {
                currentBarsColor to currentIsBarLight
            }
        }
        
        // Determine navigation bar configuration
        val (navigationBarColor, navigationBarDarkIcons) = when {
            // Priority 1: Custom navigation bar color
            isCustomColorEnable && customStatusColor != null -> {
                customStatusColor.navigationBar to (customStatusColor.navigationBar.luminance() > 0.5)
            }
            // Priority 2: Default bars color (even with transparent status bar)
            else -> {
                currentBarsColor to currentIsBarLight
            }
        }
        
        // Apply status bar color
        systemUiController?.setStatusBarColor(
            color = statusBarColor,
            darkIcons = statusBarDarkIcons,
        )
        
        // Apply navigation bar color
        systemUiController?.setNavigationBarColor(
            color = navigationBarColor,
            darkIcons = navigationBarDarkIcons,
            navigationBarContrastEnforced = false
        )
        
        onDispose {
            // Reset to default on dispose
            systemUiController?.setSystemBarsColor(
                color = currentBarsColor,
                darkIcons = currentIsBarLight,
                isNavigationBarContrastEnforced = false
            )
        }
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
