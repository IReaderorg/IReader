package ireader.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

import ireader.ui.component.Controller
import ireader.ui.component.components.TitleToolbar
import ireader.core.ui.ui.SnackBarListener
import ireader.ui.settings.backups.BackUpAndRestoreScreen
import ireader.ui.settings.backups.BackupScreenViewModel
import ireader.presentation.R
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

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
        val vm: BackupScreenViewModel = getViewModel(owner = controller.navBackStackEntry)
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
