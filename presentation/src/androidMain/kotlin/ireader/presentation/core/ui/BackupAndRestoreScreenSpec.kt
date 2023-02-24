package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.presentation.R
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.backups.BackUpAndRestoreScreen
import ireader.presentation.ui.settings.backups.BackupScreenViewModel
import org.koin.androidx.compose.getViewModel

class BackupAndRestoreScreenSpec : VoyagerScreen() {




    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val vm: BackupScreenViewModel = getIViewModel()
        val snackBarHostState = SnackBarListener(vm = vm)
        val navigator = LocalNavigator.currentOrThrow
        IScaffold(
            topBar = {scrollBehavior ->
                TitleToolbar(
                    title = stringResource(R.string.backup_and_restore),
                    scrollBehavior = scrollBehavior,
                    popBackStack = {
                        popBackStack(navigator)
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
                    popBackStack(navigator)
                },
                snackbarHostState = snackBarHostState,
                scaffoldPadding = padding
            )
        }

    }
}
