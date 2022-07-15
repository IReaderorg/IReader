package org.ireader.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.ireader.components.Controller
import org.ireader.components.components.TitleToolbar
import org.ireader.core_ui.ui.SnackBarListener
import org.ireader.settings.setting.backups.BackUpAndRestoreScreen
import org.ireader.settings.setting.backups.BackupScreenViewModel
import org.ireader.ui_settings.R

object BackupAndRestoreScreenSpec : ScreenSpec {

    override val navHostRoute: String = "backup_restore"

    @ExperimentalMaterial3Api
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        TitleToolbar(
            title = stringResource(R.string.backup_and_restore),
            navController = controller.navController,
            scrollBehavior = controller.scrollBehavior
        )
    }

    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: BackupScreenViewModel = hiltViewModel(controller.navBackStackEntry)
        SnackBarListener(vm = vm, host = controller.snackBarHostState)
        BackUpAndRestoreScreen(
            modifier = Modifier.padding(controller.scaffoldPadding),
            vm = vm,
            onBackStack =
            {
                controller.navController.popBackStack()
            },
            snackbarHostState = controller.snackBarHostState
        )
    }
}
