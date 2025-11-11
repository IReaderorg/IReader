package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.services.tts_service.DesktopTTSService

@Composable
fun DesktopTTSControlPanel(
    ttsService: DesktopTTSService,
    modifier: Modifier = Modifier
) {
    val state = ttsService.state
    var showVoiceSettings by remember { mutableStateOf(false) }
    
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Progress indicator
            state.ttsContent?.value?.let { content ->
                if (content.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Paragraph ${state.currentReadingParagraph + 1} / ${content.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            state.selectedVoiceModel?.let { model ->
                                Text(
                                    text = model.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        LinearProgressIndicator(
                            progress = (state.currentReadingParagraph + 1).toFloat() / content.size,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Chapter
                IconButton(
                    onClick = { ttsService.startService(DesktopTTSService.ACTION_SKIP_PREV) },
                    enabled = state.ttsChapter != null
                ) {
                    Icon(Icons.Default.SkipPrevious, "Previous Chapter")
                }
                
                // Previous Paragraph
                IconButton(
                    onClick = { ttsService.startService(DesktopTTSService.ACTION_PREV_PAR) },
                    enabled = state.currentReadingParagraph > 0
                ) {
                    Icon(Icons.Default.FastRewind, "Previous Paragraph")
                }
                
                // Play/Pause (Large button)
                FilledIconButton(
                    onClick = {
                        if (state.isPlaying) {
                            ttsService.startService(DesktopTTSService.ACTION_PAUSE)
                        } else {
                            ttsService.startService(DesktopTTSService.ACTION_PLAY)
                        }
                    },
                    enabled = state.ttsChapter != null,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Next Paragraph
                IconButton(
                    onClick = { ttsService.startService(DesktopTTSService.ACTION_NEXT_PAR) },
                    enabled = state.ttsContent?.value?.let { 
                        state.currentReadingParagraph < it.lastIndex 
                    } ?: false
                ) {
                    Icon(Icons.Default.FastForward, "Next Paragraph")
                }
                
                // Next Chapter
                IconButton(
                    onClick = { ttsService.startService(DesktopTTSService.ACTION_SKIP_NEXT) },
                    enabled = state.ttsChapter != null
                ) {
                    Icon(Icons.Default.SkipNext, "Next Chapter")
                }
            }
            
            // Speed and Auto-next controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speed control
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Speed",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = String.format("%.1fx", state.speechSpeed),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Slider(
                        value = state.speechSpeed,
                        onValueChange = { ttsService.setSpeechRate(it) },
                        valueRange = 0.5f..2.0f,
                        steps = 14,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Auto-next chapter
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Auto-next",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Switch(
                        checked = state.autoNextChapter,
                        onCheckedChange = { /* Update via preferences */ }
                    )
                }
            }
            
            // Voice settings button
            OutlinedButton(
                onClick = { showVoiceSettings = !showVoiceSettings },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (showVoiceSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (showVoiceSettings) "Hide Voice Settings" else "Show Voice Settings")
            }
            
            // Voice settings panel (collapsible)
            if (showVoiceSettings) {
                VoiceModelManagementPanel(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
