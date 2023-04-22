package ireader.presentation.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

actual class IUseController {


    actual fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean,
    ) = setIStatusBarColor(color, darkIcons)

    fun setIStatusBarColor(
        color: Color,
        darkIcons: Boolean = color.luminance() > 0.5f,
        isNavigationBarContrastEnforced: Boolean = true,
        transformColorForLightContent: (Color) -> Color = BlackScrimmed
    ) {
        val systemUiController = controller as SystemUiController
        systemUiController.setSystemBarsColor(
            color = color,
            darkIcons = darkIcons,
            isNavigationBarContrastEnforced = false
        )
    }

    actual fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean,
        navigationBarContrastEnforced: Boolean,
    ) = setINavigationBarColor(color, darkIcons, navigationBarContrastEnforced)

    fun setINavigationBarColor(
        color: Color,
        darkIcons: Boolean = color.luminance() > 0.5f,
        navigationBarContrastEnforced: Boolean = true,
        transformColorForLightContent: (Color) -> Color = BlackScrimmed
    ) {
        val systemUiController = controller as SystemUiController
        systemUiController.setNavigationBarColor(
            color, darkIcons, navigationBarContrastEnforced, transformColorForLightContent
        )
    }

    actual fun setSystemBarsColor(
        color: Color,
        darkIcons: Boolean,
        isNavigationBarContrastEnforced: Boolean,
    ) = setISystemBarsColor(
        color,
        darkIcons,
        isNavigationBarContrastEnforced,
    )

    fun setISystemBarsColor(
        color: Color,
        darkIcons: Boolean = color.luminance() > 0.5f,
        isNavigationBarContrastEnforced: Boolean = true,
        transformColorForLightContent: (Color) -> Color = BlackScrimmed
    ) {
        val systemUiController = controller as SystemUiController
        systemUiController.setSystemBarsColor(
            color,
            darkIcons,
            isNavigationBarContrastEnforced,
            transformColorForLightContent
        )
    }

    private val BlackScrim = Color(0f, 0f, 0f, 0.3f) // 30% opaque black
    private val BlackScrimmed: (Color) -> Color = { original ->
        BlackScrim.compositeOver(original)
    }


    actual var controller: Any? = null


    actual fun enableImmersiveModel() {
        val systemUiController = controller as SystemUiController
        systemUiController.isStatusBarVisible = false
        systemUiController.isSystemBarsVisible = false
        systemUiController.isNavigationBarVisible = false
    }

    actual fun disableImmersiveModel() {
        val systemUiController = controller as SystemUiController
        systemUiController.isStatusBarVisible = true
        systemUiController.isSystemBarsVisible = true
        systemUiController.isNavigationBarVisible = true
    }

    @Composable
    actual fun InitController() {
        if (controller == null) {
            controller = rememberSystemUiController()
        }
    }


}