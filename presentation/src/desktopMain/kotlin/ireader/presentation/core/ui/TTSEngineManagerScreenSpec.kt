package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator

import androidx.compose.runtime.Composable
import ireader.domain.plugins.RequiredPluginChecker
import ireader.presentation.ui.settings.TTSEngineManagerScreen
import org.koin.compose.koinInject
import ireader.presentation.core.safePopBackStack
/**
 * Desktop implementation of TTS Engine Manager Screen
 * Manages TTS engines (Piper, Kokoro)
 * 
 * On desktop, Piper TTS requires a plugin to be installed.
 * The RequiredPluginHandler will be shown when user tries to install/use Piper
 * and the plugin is not available.
 */
actual class TTSEngineManagerScreenSpec {
    
    @Composable
    actual fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val requiredPluginChecker: RequiredPluginChecker = koinInject()
        
        TTSEngineManagerScreen(
            onNavigateBack = {
                navController.safePopBackStack()
            },
            onRequestPiperPlugin = {
                // Show RequiredPluginHandler when user tries to use Piper
                if (!requiredPluginChecker.isPiperTTSAvailable()) {
                    requiredPluginChecker.requestPiperTTS()
                }
            },
            isPiperPluginAvailable = requiredPluginChecker.isPiperTTSAvailable()
        )
    }
}
