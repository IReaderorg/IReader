package ireader.presentation.ui.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import ireader.presentation.core.toComposeColor

/**
 * Helper composable to ensure proper system bars colors on screen navigation.
 * Particularly useful for detail screens and other destinations.
 * 
 * This helps prevent the white status bar issue on Xiaomi devices.
 */
@Composable
fun SystemBarsEffect(
    statusBarColor: Color? = null,
    navigationBarColor: Color? = null,
    enforceContrast: Boolean = false
) {
    val systemUiController = LocalISystemUIController.current
    val appColors = AppColors.current
    
    DisposableEffect(statusBarColor, navigationBarColor) {
        val finalStatusBarColor = statusBarColor ?: appColors.bars.toComposeColor()
        val finalNavigationBarColor = navigationBarColor ?: appColors.bars.toComposeColor()
        
        val statusBarDarkIcons = finalStatusBarColor.luminance() > 0.5
        val navigationBarDarkIcons = finalNavigationBarColor.luminance() > 0.5
        
        systemUiController?.setStatusBarColor(
            color = finalStatusBarColor,
            darkIcons = statusBarDarkIcons
        )
        
        systemUiController?.setNavigationBarColor(
            color = finalNavigationBarColor,
            darkIcons = navigationBarDarkIcons,
            navigationBarContrastEnforced = enforceContrast
        )
        
        onDispose {
            // Restore default colors when leaving the screen
            systemUiController?.setStatusBarColor(
                color = appColors.bars.toComposeColor(),
                darkIcons = appColors.isBarLight
            )
            systemUiController?.setNavigationBarColor(
                color = appColors.bars.toComposeColor(),
                darkIcons = appColors.isBarLight,
                navigationBarContrastEnforced = false
            )
        }
    }
}

/**
 * Ensures system bars are properly configured when entering a screen.
 * Use this at the top of your screen composables to prevent status bar issues.
 */
@Composable
fun EnsureSystemBars() {
    val systemUiController = LocalISystemUIController.current
    val appColors = AppColors.current
    
    DisposableEffect(appColors.bars, appColors.isBarLight) {
        // Reapply system bars colors to fix any inconsistencies
        systemUiController?.setSystemBarsColor(
            color = appColors.bars.toComposeColor(),
            darkIcons = appColors.isBarLight,
            isNavigationBarContrastEnforced = false
        )
        
        onDispose { }
    }
}
