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

    @Composable
    actual fun InitController() {
    }


}