package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator

import androidx.compose.runtime.Composable
import ireader.presentation.ui.settings.TTSEngineManagerScreen

/**
 * Desktop implementation of TTS Engine Manager Screen
 */
actual class TTSEngineManagerScreenSpec {
    
    @Composable
    actual fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        
        TTSEngineManagerScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
}
