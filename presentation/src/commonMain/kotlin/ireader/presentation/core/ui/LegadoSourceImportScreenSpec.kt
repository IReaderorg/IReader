package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.sourcecreator.legado.LegadoSourceImportScreen
import ireader.presentation.ui.sourcecreator.legado.LegadoSourceImportViewModel
import org.koin.compose.koinInject
import ireader.presentation.core.safePopBackStack
/**
 * Screen spec for importing Legado/阅读 format sources.
 */
class LegadoSourceImportScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val vm: LegadoSourceImportViewModel = koinInject()
        val state by vm.state.collectAsState()
        val navController = LocalNavigator.current
        val snackbarHostState = remember { SnackbarHostState() }
        
        // Show snackbar messages
        LaunchedEffect(state.snackbarMessage) {
            state.snackbarMessage?.let {
                snackbarHostState.showSnackbar(it)
                vm.clearSnackbar()
            }
        }
        
        // Navigate back on successful import
        LaunchedEffect(state.importSuccess) {
            if (state.importSuccess) {
                navController?.safePopBackStack()
            }
        }
        
        LegadoSourceImportScreen(
            state = state,
            onBack = { navController?.safePopBackStack() },
            onUrlChange = { vm.updateSourceUrl(it) },
            onJsonChange = { vm.updateJsonContent(it) },
            onFetchFromUrl = { vm.fetchFromUrl() },
            onFetchFromRepository = { vm.fetchFromRepository(it) },
            onParseJson = { vm.parseFromJson() },
            onToggleJsonInput = { vm.toggleJsonInput() },
            onToggleSource = { vm.toggleSourceSelection(it) },
            onSelectAll = { vm.selectAll() },
            onDeselectAll = { vm.deselectAll() },
            onImport = { vm.importSelected() },
            onClearParsed = { vm.clearParsedSources() },
            snackbarHostState = snackbarHostState
        )
    }
}
