package ireader.presentation.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

actual class IUseController {

    actual fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean,
    ) {
        // iOS handles status bar styling differently through UIKit
    }

    actual fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean,
        navigationBarContrastEnforced: Boolean,
    ) {
        // iOS doesn't have a navigation bar like Android
    }

    actual fun setSystemBarsColor(
        color: Color,
        darkIcons: Boolean,
        isNavigationBarContrastEnforced: Boolean,
    ) {
        // iOS handles system bars differently
    }

    actual var controller: Any? = null

    actual fun enableImmersiveModel() {
        // iOS immersive mode would be handled through UIKit
    }

    actual fun disableImmersiveModel() {
        // iOS immersive mode would be handled through UIKit
    }

    @Composable
    actual fun InitController() {
        // No-op for iOS
    }
}

@Composable
actual fun StatusBarColorController(
    statusBarColor: Color,
    isDark: Boolean
) {
    // iOS status bar is controlled through UIKit
}

/**
 * Set the system bars color
 */
@Composable
actual fun SystemBarColorController(
    systemBarColor: Color,
    isDark: Boolean
) {
    // iOS system bars are controlled through UIKit
}
