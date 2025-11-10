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

/**
 * Desktop TTS control panel
 */
@Composable
fun DesktopTTSControls(
    ttsService: DesktopTTSService,
    modifier: Modifier = Modifier
) {
    val state = ttsService.state
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Text(
                text = "Text-to-Speech",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Book and Chapter info
            state.ttsBook?.let { book ->
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            state.ttsChapter?.let { chapter ->
                Text(
                    text = chapter.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Progress
            state.ttsContent?.value?.let { content ->
                if (content.isNotEmpty()) {
                    val progress = state.currentReadingParagraph.toFloat() / content.size
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Paragraph ${state.currentReadingParagraph + 1} / ${content.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Control buttons
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
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Chapter"
                    )
                }
                
                // Previous Paragraph
                IconButton(
                    onClick = { ttsService.startService(DesktopTTSService.ACTION_PREV_PAR) },
                    enabled = state.currentReadingParagraph > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Previous Paragraph"
                    )
                }
                
                // Play/Pause
                IconButton(
                    onClick = {
                        if (state.isPlaying) {
                            ttsService.startService(DesktopTTSService.ACTION_PAUSE)
                        } else {
                            ttsService.startService(DesktopTTSService.ACTION_PLAY)
                        }
                    },
                    enabled = state.ttsChapter != null
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.primary,
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
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Next Paragraph"
                    )
                }
                
                // Next Chapter
                IconButton(
                    onClick = { ttsService.startService(DesktopTTSService.ACTION_SKIP_NEXT) },
                    enabled = state.ttsChapter != null
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Chapter"
                    )
                }
                
                // Stop
                IconButton(
                    onClick = { ttsService.startService(DesktopTTSService.ACTION_STOP) },
                    enabled = state.ttsChapter != null
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Settings
            var showSettings by remember { mutableStateOf(false) }
            
            // Settings row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Speed: ${String.format("%.1f", state.speechSpeed)}x",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    // Speed slider
                    Slider(
                        value = state.speechSpeed,
                        onValueChange = { /* Will be updated via preferences */ },
                        valueRange = 0.5f..2.0f,
                        steps = 14,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Auto-next",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Switch(
                        checked = state.autoNextChapter,
                        onCheckedChange = { /* Will be updated via preferences */ }
                    )
                }
            }
        }
    }
}

/**
 * Compact TTS indicator for the reader screen
 */
@Composable
fun DesktopTTSIndicator(
    ttsService: DesktopTTSService,
    modifier: Modifier = Modifier
) {
    val state = ttsService.state
    
    if (state.isPlaying) {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Reading",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                
                Text(
                    text = "Reading aloud",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
