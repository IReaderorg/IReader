package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ireader.presentation.R
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.backups.BackUpAndRestoreScreen
import ireader.presentation.ui.settings.backups.BackupScreenViewModel
import org.koin.androidx.compose.getViewModel

object BackupAndRestoreScreenSpec : ScreenSpec {

    override val navHostRoute: String = "backup_restore"


    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
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
        val vm: BackupScreenViewModel = getViewModel(viewModelStoreOwner = controller.navBackStackEntry)
        SnackBarListener(vm = vm, host = controller.snackBarHostState)
        BackUpAndRestoreScreen(
            modifier = Modifier.padding(controller.scaffoldPadding),
            vm = vm,
            onBackStack =
            {
                controller.navController.popBackStack()
            },
            snackbarHostState = controller.snackBarHostState,
            scaffoldPadding = controller.scaffoldPadding
        )
    }
}
