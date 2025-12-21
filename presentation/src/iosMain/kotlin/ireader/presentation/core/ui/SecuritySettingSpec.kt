package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.i18n.resources.*
import ireader.i18n.resources.security
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.theme.currentOrThrow
import ireader.presentation.ui.settings.security.SecuritySettingsScreen
import ireader.presentation.ui.settings.security.SecuritySettingsViewModel
import ireader.presentation.core.safePopBackStack
actual class SecuritySettingSpec {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    actual fun Content() {
        val vm: SecuritySettingsViewModel = getIViewModel()
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val localizeHelper = LocalLocalizeHelper.currentOrThrow
        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = localizeHelper.localize(Res.string.security),
                    scrollBehavior = scrollBehavior,
                    popBackStack = {
                        navController.safePopBackStack()
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
