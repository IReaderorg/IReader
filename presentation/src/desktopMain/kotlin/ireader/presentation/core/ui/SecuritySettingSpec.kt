package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.security.SecuritySettingsScreen
import ireader.presentation.ui.settings.security.SecuritySettingsViewModel

actual class SecuritySettingSpec : VoyagerScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val vm: SecuritySettingsViewModel = getIViewModel()
        val navigator = LocalNavigator.currentOrThrow

        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = "Security",
                    scrollBehavior = scrollBehavior,
                    popBackStack = {
                        popBackStack(navigator)
                    }
                )
            }
        ) { padding ->
            SecuritySettingsScreen(
                vm = vm,
                padding = padding
            )
        }
    }
}