package ireader.presentation.core.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.R
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.settings.general.GeneralSettingScreen
import ireader.presentation.ui.settings.general.GeneralSettingScreenViewModel
import org.koin.androidx.compose.getViewModel

@ExperimentalMaterial3Api
@OptIn(ExperimentalMaterialApi::class)
class GeneralScreenSpec : VoyagerScreen() {

    @Composable
    override fun Content() {
        val vm: GeneralSettingScreenViewModel = getIViewModel()
        val navigator = LocalNavigator.currentOrThrow
        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = stringResource(id = R.string.general),
                    scrollBehavior = scrollBehavior,
                    popBackStack = {
                        popBackStack(navigator)
                    }
                )
            }
        ) {scaffoldPadding ->
            GeneralSettingScreen(
                scaffoldPadding = scaffoldPadding,
                vm = vm,
            )
        }

    }
}
