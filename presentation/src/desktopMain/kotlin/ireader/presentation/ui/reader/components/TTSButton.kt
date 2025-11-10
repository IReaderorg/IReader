package ireader.presentation.ui.reader.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import ireader.domain.services.tts_service.DesktopTTSService
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import org.koin.compose.koinInject

/**
 * Desktop TTS button for the reader toolbar
 */
@Composable
fun TTSButton(
    onToggleTTSControls: () -> Unit,
    ttsService: DesktopTTSService = koinInject()
) {
    val state = ttsService.state
    
    AppIconButton(
        imageVector = when {
            state.isPlaying -> Icons.Default.Pause
            state.ttsChapter != null -> Icons.Default.PlayArrow
            else -> Icons.Default.VolumeUp
        },
        contentDescription = "Text-to-Speech",
        tint = if (state.isPlaying) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.onBackground,
        onClick = {
            if (state.isPlaying) {
                ttsService.startService(DesktopTTSService.ACTION_PAUSE)
            } else if (state.ttsChapter != null) {
                ttsService.startService(DesktopTTSService.ACTION_PLAY)
            } else {
                // Show controls to load chapter
                onToggleTTSControls()
            }
        }
    )
}
