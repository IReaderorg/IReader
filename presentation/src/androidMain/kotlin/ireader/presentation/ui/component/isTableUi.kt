package ireader.presentation.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import ireader.domain.utils.isTabletUi

@Composable
actual fun isTableUi(): Boolean {
    return LocalContext.current.isTabletUi()
}

@Composable
actual fun isLandscape(): Boolean {
    val window = LocalConfiguration.current
    return window.screenWidthDp > window.screenHeightDp
}
