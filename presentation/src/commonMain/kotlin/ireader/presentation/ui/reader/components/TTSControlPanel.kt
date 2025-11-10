package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * TTS control panel with playback controls and speed adjustment
 * Requirements: 10.1, 10.2, 10.3, 10.4
 */
@Composable
fun TTSControlPanel(
    isPlaying: Boolean,
    currentPosition: Duration,
    duration: Duration,
    speechRate: Float,
    onPlayPause: () -> Unit,
    onSkipBackward: () -> Unit,
    onSkipForward: () -> Unit,
    onSpeechRateChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = if (duration.inWholeMilliseconds > 0) {
                    (currentPosition.inWholeMilliseconds.toFloat() / duration.inWholeMilliseconds.toFloat())
                        .coerceIn(0f, 1f)
                } else {
                    0f
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Time display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(currentPosition),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDuration(duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip backward button
                IconButton(
                    onClick = onSkipBackward,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Skip backward 10 seconds",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Play/Pause button
                FilledIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Skip forward button
                IconButton(
                    onClick = onSkipForward,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Skip forward 10 seconds",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Speed control
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Speed,
                    contentDescription = "Speed",
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Slider(
                    value = speechRate,
                    onValueChange = onSpeechRateChanged,
                    valueRange = 0.5f..2.0f,
                    steps = 14, // 0.5, 0.6, 0.7, ..., 1.9, 2.0
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "${(speechRate * 100).toInt()}%",
                    modifier = Modifier.width(50.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Format duration in MM:SS format
 */
fun formatDuration(duration: Duration): String {
    val totalSeconds = duration.inWholeSeconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
