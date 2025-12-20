package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.appearance.AppearanceSettingScreen
import ireader.presentation.ui.settings.appearance.AppearanceToolbar
import ireader.presentation.ui.settings.appearance.AppearanceViewModel
import ireader.presentation.core.safePopBackStack
class AppearanceScreenSpec {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val viewModel: AppearanceViewModel = getIViewModel()
        val host = SnackBarListener(viewModel)
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        IScaffold(
            topBar = { scrollBehavior ->
                AppearanceToolbar(
                    vm = viewModel,
                    onPopBackStack = {
                        navController.safePopBackStack()
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            snackbarHostState = host
        ) { padding ->
            AppearanceSettingScreen(
                modifier = Modifier.padding(padding),
                saveDarkModePreference = { theme ->
                    viewModel.saveNightModePreferences(theme)
                },
                onPopBackStack = {
                    navController.safePopBackStack()
                },
                vm = viewModel,
                scaffoldPaddingValues = padding,
                onColorChange = {
                    viewModel.isSavable = true
                },
                onColorReset = {
                    viewModel.isSavable = false
                }

            )
        }

    }
}
