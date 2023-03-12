package ireader.presentation.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ireader.domain.utils.isTabletUi

@Composable
actual fun isTableUi(): Boolean {
    return LocalContext.current.isTabletUi()
}