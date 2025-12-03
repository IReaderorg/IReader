package ireader.presentation.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

/**
 * Common interface for system UI control across platforms
 */
expect class IUseController() {
    /**
     * Set the status bar color
     */
    fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean,
    )

    /**
     * Set the navigation bar color
     */
    fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean,
        navigationBarContrastEnforced: Boolean = true,
    )

    /**
     * Set both status bar and navigation bar color 
     */
    fun setSystemBarsColor(
        color: Color,
        darkIcons: Boolean,
        isNavigationBarContrastEnforced: Boolean = true,
    )

    /**
     * Enable immersive mode (hide system bars)
     */
    fun enableImmersiveModel()

    /**
     * Disable immersive mode (show system bars)
     */
    fun disableImmersiveModel()

    /**
     * For internal use - the platform-specific controller
     */
    var controller: Any?

    /**
     * Initialize the platform-specific controller
     */
    @Composable
    fun InitController()

}

/**
 * Set the status bar color
 */
@Composable
expect fun StatusBarColorController(
    statusBarColor: Color,
    isDark: Boolean
)

/**
 * Set the system bars color
 */
@Composable
expect fun SystemBarColorController(
    systemBarColor: Color,
    isDark: Boolean
)
