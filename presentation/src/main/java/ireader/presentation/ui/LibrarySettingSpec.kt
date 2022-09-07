package ireader.presentation.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ireader.common.resources.R
import ireader.ui.component.Controller
import ireader.ui.component.components.TitleToolbar
import org.koin.androidx.compose.get
object LibrarySettingSpec : ScreenSpec {

    override val navHostRoute: String = "library_settings_screen_route"

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        TitleToolbar(
            title = stringResource(R.string.library),
            navController = controller.navController,
            scrollBehavior = controller.scrollBehavior
        )
    }

    @Composable
    override fun Content(
        controller: Controller
    ) {
    }
}
