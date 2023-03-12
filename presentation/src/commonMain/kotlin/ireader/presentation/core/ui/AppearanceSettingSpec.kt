package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.appearance.AppearanceSettingScreen
import ireader.presentation.ui.settings.appearance.AppearanceToolbar
import ireader.presentation.ui.settings.appearance.AppearanceViewModel

class AppearanceScreenSpec : VoyagerScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel: AppearanceViewModel = getIViewModel()
        val host = SnackBarListener(viewModel)
        val navigator = LocalNavigator.currentOrThrow
        IScaffold(
            topBar = { scrollBehavior ->
                AppearanceToolbar(
                    vm = viewModel,
                    onPopBackStack = {
                        popBackStack(navigator)
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
                    popBackStack(navigator)
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

fun popBackStack(navController: Navigator) {
    navController.pop()
}
