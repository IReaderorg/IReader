package ireader.presentation.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.app.Activity
import android.view.View
import android.view.Window

/**
 * Modern replacement for SystemUiController using androidx.core WindowCompat APIs
 */
class WindowInsetsController(private val window: Window) {
    private val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    
    /**
     * Set the color of system bars (both status bar and navigation bar)
     */
    fun setSystemBarsColor(
        color: Color,
        darkIcons: Boolean = false,
        isNavigationBarContrastEnforced: Boolean = false
    ) {
        window.statusBarColor = color.toArgb()
        window.navigationBarColor = color.toArgb()
        
        insetsController.isAppearanceLightStatusBars = darkIcons
        insetsController.isAppearanceLightNavigationBars = darkIcons
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
    
    /**
     * Set navigation bar color
     */
    fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean = false,
        navigationBarContrastEnforced: Boolean = false
    ) {
        window.navigationBarColor = color.toArgb()
        insetsController.isAppearanceLightNavigationBars = darkIcons
    }
    
    /**
     * Set status bar color
     */
    fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean = false,
    ) {
        window.statusBarColor = color.toArgb()
        insetsController.isAppearanceLightStatusBars = darkIcons
    }
    
    var isStatusBarVisible: Boolean
        get() = true // Always return true as we can't directly query this
        set(value) {
            if (value) {
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            } else {
                insetsController.hide(WindowInsetsCompat.Type.statusBars())
            }
        }

    var isNavigationBarVisible: Boolean
        get() = true // Always return true as we can't directly query this
        set(value) {
            if (value) {
                insetsController.show(WindowInsetsCompat.Type.navigationBars())
            } else {
                insetsController.hide(WindowInsetsCompat.Type.navigationBars())
            }
        }

    var isSystemBarsVisible: Boolean
        get() = true // Always return true as we can't directly query this
        set(value) {
            isStatusBarVisible = value
            isNavigationBarVisible = value
        }
    
    fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}

/**
 * Remember a WindowInsetsController for the current view
 */
@Composable
fun rememberWindowInsetsController(): WindowInsetsController {
    val context = LocalContext.current
    val view = LocalView.current
    val window = (context as Activity).window
    
    return remember { WindowInsetsController(window) }
}

/**
 * Set the status bar color
 */
@Composable
actual fun StatusBarColorController(statusBarColor: Color, isDark: Boolean) {
    val controller = rememberWindowInsetsController()
    
    DisposableEffect(statusBarColor, isDark) {
        controller.setStatusBarColor(
            color = statusBarColor,
            darkIcons = !isDark
        )
        onDispose {}
    }
}

/**
 * Set the system bars color
 */
@Composable
actual fun SystemBarColorController(systemBarColor: Color, isDark: Boolean) {
    val controller = rememberWindowInsetsController()
    
    DisposableEffect(systemBarColor, isDark) {
        controller.setSystemBarsColor(
            color = systemBarColor,
            darkIcons = !isDark
        )
        onDispose {}
    }
}

private fun Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (alpha * 255.0f + 0.5f).toInt(),
        (red * 255.0f + 0.5f).toInt(),
        (green * 255.0f + 0.5f).toInt(),
        (blue * 255.0f + 0.5f).toInt()
    )
}

/**
 * Android implementation of IUseController using WindowInsetsController
 */
actual class IUseController {
    actual var controller: Any? = null
    
    private val windowInsetsController: WindowInsetsController?
        get() = controller as? WindowInsetsController
    
    actual fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean,
    ) {
        windowInsetsController?.setStatusBarColor(color, darkIcons)
    }
    
    actual fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean,
        navigationBarContrastEnforced: Boolean,
    ) {
        windowInsetsController?.setNavigationBarColor(
            color = color,
            darkIcons = darkIcons,
            navigationBarContrastEnforced = navigationBarContrastEnforced
        )
    }
    
    actual fun setSystemBarsColor(
        color: Color,
        darkIcons: Boolean,
        isNavigationBarContrastEnforced: Boolean,
    ) {
        windowInsetsController?.setSystemBarsColor(
            color = color,
            darkIcons = darkIcons,
            isNavigationBarContrastEnforced = isNavigationBarContrastEnforced
        )
    }
    
    actual fun enableImmersiveModel() {
        windowInsetsController?.isSystemBarsVisible = false
    }
    
    actual fun disableImmersiveModel() {
        windowInsetsController?.isSystemBarsVisible = true
    }
    /**
     * Initialize the controller with a WindowInsetsController
     */
    @Composable
    actual fun InitController() {
        val context = LocalContext.current
        val activity = context as Activity
        val window = activity.window
        controller = remember { WindowInsetsController(window) }
    }

}


