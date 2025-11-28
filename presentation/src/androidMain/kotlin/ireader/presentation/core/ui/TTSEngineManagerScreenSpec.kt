package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.settings.AndroidTTSMManagerSettingsScreen
import ireader.presentation.ui.settings.viewmodels.AITTSSettingsViewModel

/**
 * Android implementation of TTS Engine Manager Screen
 * Provides information about native Android TTS and Sherpa TTS app recommendation
 */
actual class TTSEngineManagerScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    actual fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: AITTSSettingsViewModel = getIViewModel()
        
        IScaffold { padding ->
            AndroidTTSMManagerSettingsScreen(
                onBackPressed = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }
    }
}
