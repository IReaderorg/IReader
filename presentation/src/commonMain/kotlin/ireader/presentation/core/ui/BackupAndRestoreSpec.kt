package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.backups.BackUpAndRestoreScreen
import ireader.presentation.ui.settings.backups.BackupScreenViewModel

class BackupAndRestoreScreenSpec {




    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val vm: BackupScreenViewModel = getIViewModel()
        val snackBarHostState = SnackBarListener(vm = vm)
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        IScaffold(
            topBar = {scrollBehavior ->
                TitleToolbar(
                    title = localize(Res.string.backup_and_restore),
                    scrollBehavior = scrollBehavior,
                    popBackStack = {
                        navController.popBackStack()
                    }
                )
            },
            snackbarHostState = snackBarHostState
        ) { padding ->
            BackUpAndRestoreScreen(
                modifier = Modifier.padding(padding),
                vm = vm,
                onBackStack =
                {
                    navController.popBackStack()
                },
                snackbarHostState = snackBarHostState,
                scaffoldPadding = padding,
                onNavigateToCloudBackup = {
                    navController.navigate(NavigationRoutes.cloudBackup)
                }
            )
        }

    }
}
