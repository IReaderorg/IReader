package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.settings.sync.UnifiedSyncScreen
import ireader.presentation.ui.settings.sync.UnifiedSyncViewModel


actual class SyncScreenSpec {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    actual fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: UnifiedSyncViewModel = getIViewModel()
        val state by viewModel.state.collectAsState()
        
        UnifiedSyncScreen(
            state = state,
            onNavigateUp = {
                navController.safePopBackStack()
            },
            onSelectProvider = { viewModel.setProvider(it) },
            onSyncNow = { viewModel.syncNow() },
            onCancelSync = { viewModel.cancelSync() },
            onToggleAutoSyncOnLaunch = { viewModel.toggleAutoSyncOnLaunch(it) },
            onToggleAutoSyncOnChapterFinish = { viewModel.toggleAutoSyncOnChapterFinish(it) },
            onToggleSyncOnWifiOnly = { viewModel.toggleSyncOnWifiOnly(it) }
        )
    }
}

