package ireader.presentation.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

actual class IUseController {

    actual fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean,
    ) {
    }


    actual fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean,
        navigationBarContrastEnforced: Boolean,
    ) {
    }


    actual fun setSystemBarsColor(
        color: Color,
        darkIcons: Boolean,
        isNavigationBarContrastEnforced: Boolean,
    ) {
    }

    actual var controller: Any? = null



    actual fun enableImmersiveModel() {
    }

    actual fun disableImmersiveModel() {
    }
    @Composable
    actual fun InitController() {
    }


}
@Composable
actual fun StatusBarColorController(
    statusBarColor: Color,
    isDark: Boolean
){}

/**
 * Set the system bars color
 */
@Composable
actual fun SystemBarColorController(
    systemBarColor: Color,
    isDark: Boolean
) {}
