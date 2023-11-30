package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.i18n.localize

import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.backups.BackUpAndRestoreScreen
import ireader.presentation.ui.settings.backups.BackupScreenViewModel

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
                    title = localize() { xml ->
                        xml.backupAndRestore
                    },
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