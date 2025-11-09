package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.settings.about.ChangelogScreen

class ChangelogScreenSpec : VoyagerScreen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        ChangelogScreen(
            onPopBackStack = { popBackStack(navigator) }
        )
    }
}
