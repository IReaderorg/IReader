package ireader.presentation.core.ui

import androidx.compose.runtime.Composable

/**
 * TTS Engine Manager Screen - Desktop only
 * 
 * This screen allows users to install and manage TTS engines.
 * Only available on desktop platform.
 */
expect class TTSEngineManagerScreenSpec() {
    @Composable
    fun Content()
}
