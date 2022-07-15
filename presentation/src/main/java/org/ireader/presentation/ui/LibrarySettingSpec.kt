package org.ireader.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.ireader.components.Controller
import org.ireader.common_resources.R
import org.ireader.components.components.TitleToolbar

object LibrarySettingSpec : ScreenSpec {

    override val navHostRoute: String = "library_settings_screen_route"

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
