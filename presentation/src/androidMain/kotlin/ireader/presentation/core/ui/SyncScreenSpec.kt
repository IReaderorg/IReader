package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.sync.SyncScreen

actual class SyncScreenSpec {

    @Composable
    actual fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        SyncScreen(
            onNavigateBack = {
                navController.safePopBackStack()
            }
        )
    }
}


