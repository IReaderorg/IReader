package ireader.presentation.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.services.tts_service.DesktopTTSService
import kotlinx.coroutines.launch

/**
 * TTS Performance Settings Dialog
 * Allows users to configure performance options for TTS engines
 */
@Composable
fun TTSPerformanceSettingsDialog(
    ttsService: DesktopTTSService,
    onDismiss: () -> Unit
) {
    var maxProcesses by remember { mutableStateOf(ttsService.getMaxConcurrentProcesses()) }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("TTS Performance Settings")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Max Concurrent Processes Setting
                Text(
                    text = "Maximum Concurrent Processes",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = "Controls how many audio files can be generated simultaneously for Kokoro and Maya engines. Higher values = faster generation but more CPU/memory usage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Processes: $maxProcesses",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { if (maxProcesses > 1) maxProcesses-- },
                            enabled = maxProcesses > 1
                        ) {
                            Text("-")
                        }
                        
                        FilledTonalButton(
                            onClick = { if (maxProcesses < 8) maxProcesses++ },
                            enabled = maxProcesses < 8
                        ) {
                            Text("+")
                        }
                    }
                }
                
                Slider(
                    value = maxProcesses.toFloat(),
                    onValueChange = { maxProcesses = it.toInt() },
                    valueRange = 1f..8f,
                    steps = 6,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Performance recommendations
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Column {
                            Text(
                                text = "Recommendations:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = when (maxProcesses) {
                                    1 -> "• Slowest but lowest resource usage\n• Good for older computers"
                                    2 -> "• Balanced performance (default)\n• Recommended for most users"
                                    in 3..4 -> "• Faster generation\n• Requires decent CPU"
                                    else -> "• Fastest generation\n• High CPU/memory usage\n• For powerful computers only"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                // Current engine info
                val currentEngine = ttsService.getCurrentEngine()
                if (currentEngine == DesktopTTSService.TTSEngine.KOKORO || 
                    currentEngine == DesktopTTSService.TTSEngine.MAYA) {
                    val activeProcesses = if (currentEngine == DesktopTTSService.TTSEngine.KOKORO) {
                        ttsService.kokoroAdapter.getActiveProcessCount()
                    } else {
                        0 // TODO: Add Maya process count
                    }
                    
                    Text(
                        text = "Currently active processes: $activeProcesses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        ttsService.setMaxConcurrentProcesses(maxProcesses)
                        onDismiss()
                    }
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
