package ireader.presentation.core.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.R
import ireader.presentation.ui.settings.general.GeneralSettingScreen
import ireader.presentation.ui.settings.general.GeneralSettingScreenViewModel
import org.koin.androidx.compose.getViewModel

@ExperimentalMaterial3Api
@OptIn(ExperimentalMaterialApi::class)
object GeneralScreenSpec : ScreenSpec {
    override val navHostRoute: String = "general_screen_spec"

    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        TitleToolbar(
            title = stringResource(id = R.string.general),
            navController = controller.navController,
            scrollBehavior = controller.scrollBehavior
        )
    }

    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: GeneralSettingScreenViewModel = getViewModel(viewModelStoreOwner = controller.navBackStackEntry)

        val context = LocalContext.current
        GeneralSettingScreen(
            scaffoldPadding = controller.scaffoldPadding,
            vm = vm,
        )
    }
}
