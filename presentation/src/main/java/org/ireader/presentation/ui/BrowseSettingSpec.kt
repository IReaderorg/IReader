package org.ireader.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.ireader.common_resources.R
import org.ireader.components.components.TitleToolbar
import org.ireader.Controller

object BrowseSettingSpec : ScreenSpec {

    override val navHostRoute: String = "browse_settings_screen_route"

    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        TitleToolbar(
            title = stringResource(R.string.browse),
            navController = controller.navController
        )
    }


    @Composable
    override fun Content(
        controller: Controller
    ) {

    }

}
