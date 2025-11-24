package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.settings.AITTSSettingsScreen
import ireader.presentation.ui.settings.viewmodels.AITTSSettingsViewModel

/**
 * Android implementation of TTS Engine Manager Screen
 * Manages AI TTS voices (Piper TTS voice downloads and selection)
 */
actual class TTSEngineManagerScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    actual fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: AITTSSettingsViewModel = getIViewModel()
        
        IScaffold { padding ->
            AITTSSettingsScreen(
                onBackPressed = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }
    }
}
