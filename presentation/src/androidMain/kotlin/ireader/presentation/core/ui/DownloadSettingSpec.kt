package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ireader.i18n.R
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar

object DownloadSettingSpec : ScreenSpec {

    override val navHostRoute: String = "download_settings_screen_route"

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(
        controller: Controller
    ) {
        IScaffold(topBar = {
            TitleToolbar(
                title = stringResource(R.string.download),
                navController = controller.navController,
                scrollBehavior = controller.scrollBehavior
            )
        }) {

        }
    }
}
