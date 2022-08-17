package org.ireader.presentation.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.ireader.common_resources.R
import org.ireader.components.Controller
import org.ireader.components.components.TitleToolbar

object DownloadSettingSpec : ScreenSpec {

    override val navHostRoute: String = "download_settings_screen_route"

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        TitleToolbar(
            title = stringResource(R.string.download),
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
