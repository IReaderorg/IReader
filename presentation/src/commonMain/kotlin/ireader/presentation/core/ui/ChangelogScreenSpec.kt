package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.settings.about.ChangelogScreen

class ChangelogScreenSpec {

    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        
        ChangelogScreen(
            onPopBackStack = { navController.popBackStack() }
        )
    }
}
