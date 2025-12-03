package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator

/**
 * iOS implementation of TTS Engine Manager Screen
 * iOS uses AVSpeechSynthesizer for TTS
 */
actual class TTSEngineManagerScreenSpec {
    
    @Composable
    actual fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        
        // iOS TTS is handled through AVSpeechSynthesizer
        // For now, just navigate back as iOS doesn't need engine management
        navController.popBackStack()
    }
}
