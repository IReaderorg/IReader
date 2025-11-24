package ireader.presentation.core.ui

import androidx.compose.runtime.Composable

/**
 * TTS Engine Manager Screen
 * 
 * Platform-specific implementation:
 * - Android: Manages AI TTS voices (Piper TTS voice downloads and selection)
 * - Desktop: Manages TTS engines (Piper, Kokoro, Maya installation and configuration)
 */
expect class TTSEngineManagerScreenSpec() {
    @Composable
    fun Content()
}
