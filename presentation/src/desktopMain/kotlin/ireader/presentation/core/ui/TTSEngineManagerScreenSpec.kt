package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.presentation.ui.settings.TTSEngineManagerScreen

/**
 * Desktop implementation of TTS Engine Manager Screen
 */
private class TTSEngineManagerScreenImpl : Screen {
    
    override val key: ScreenKey = "TTS_ENGINE_MANAGER"
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        TTSEngineManagerScreen(
            onNavigateBack = {
                navigator.pop()
            }
        )
    }
}

/**
 * Factory function for creating TTS Engine Manager screen
 */
actual fun TTSEngineManagerScreenSpec(): Screen {
    return TTSEngineManagerScreenImpl()
}
