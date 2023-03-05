package ireader.presentation.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


expect class IUseController() {

    var controller: Any?

    @Composable
    fun InitController()

    fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean,
    )

    fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean,
        navigationBarContrastEnforced: Boolean,
    )

    fun setSystemBarsColor(
        color: Color,
        darkIcons: Boolean,
        isNavigationBarContrastEnforced: Boolean,
    )


}