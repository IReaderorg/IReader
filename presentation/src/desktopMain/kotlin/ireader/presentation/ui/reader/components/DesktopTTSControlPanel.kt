package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.services.tts_service.DesktopTTSService
import kotlinx.coroutines.launch

@Composable
fun DesktopTTSControlPanel(
    ttsService: DesktopTTSService,
    modifier: Modifier = Modifier
) {
    val state = ttsService.state
    var showEngineMenu by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showInitDialog by remember { mutableStateOf(false) }
    var isInitializing by remember { mutableStateOf(false) }
    var initError by remember { mutableStateOf<String?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0 to 0) }
    val scope = rememberCoroutineScope()
    
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
            
            // Engine and Voice Selection Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Engine Selector
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { showEngineMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Select Engine",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (ttsService.getCurrentEngine()) {
                                DesktopTTSService.TTSEngine.PIPER -> "Piper"
                                DesktopTTSService.TTSEngine.KOKORO -> "Kokoro"
                                DesktopTTSService.TTSEngine.MAYA -> "Maya"
                                DesktopTTSService.TTSEngine.SIMULATION -> "Simulation"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showEngineMenu,
                        onDismissRequest = { showEngineMenu = false }
                    ) {
                        // Show all engines (Piper, Kokoro, Maya, Simulation)
                        listOf(
                            DesktopTTSService.TTSEngine.PIPER,
                            DesktopTTSService.TTSEngine.KOKORO,
                            DesktopTTSService.TTSEngine.MAYA,
                            DesktopTTSService.TTSEngine.SIMULATION
                        ).forEach { engine ->
                            val isAvailable = ttsService.getAvailableEngines().contains(engine)
                            
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (engine == ttsService.getCurrentEngine()) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.size(18.dp))
                                        }
                                        Text(
                                            text = when (engine) {
                                                DesktopTTSService.TTSEngine.PIPER -> "Piper TTS"
                                                DesktopTTSService.TTSEngine.KOKORO -> "Kokoro TTS" + if (!isAvailable) " (Not initialized)" else ""
                                                DesktopTTSService.TTSEngine.MAYA -> "Maya TTS" + if (!isAvailable) " (Not initialized)" else ""
                                                DesktopTTSService.TTSEngine.SIMULATION -> "Simulation"
                                            }
                                        )
                                    }
                                },
                                onClick = {
                                    showEngineMenu = false
                                    
                                    // Check if engine needs initialization
                                    if (!isAvailable && (engine == DesktopTTSService.TTSEngine.KOKORO || engine == DesktopTTSService.TTSEngine.MAYA)) {
                                        // Show initialization dialog
                                        showInitDialog = true
                                        isInitializing = true
                                        initError = null
                                        
                                        scope.launch {
                                            val result = when (engine) {
                                                DesktopTTSService.TTSEngine.KOKORO -> ttsService.kokoroAdapter.initialize()
                                                DesktopTTSService.TTSEngine.MAYA -> ttsService.mayaAdapter.initialize()
                                                else -> Result.success(Unit)
                                            }
                                            
                                            isInitializing = false
                                            
                                            if (result.isSuccess) {
                                                // Mark as available
                                                when (engine) {
                                                    DesktopTTSService.TTSEngine.KOKORO -> ttsService.kokoroAvailable = true
                                                    DesktopTTSService.TTSEngine.MAYA -> ttsService.mayaAvailable = true
                                                    else -> {}
                                                }
                                                // Switch to the engine
                                                ttsService.setEngine(engine)
                                                // Auto-close dialog on success
                                                kotlinx.coroutines.delay(500) // Brief delay to show success
                                                showInitDialog = false
                                                initError = null
                                            } else {
                                                // Show error
                                                initError = result.exceptionOrNull()?.message ?: "Initialization failed"
                                            }
                                        }
                                    } else {
                                        // Engine is available, just switch
                                        ttsService.setEngine(engine)
                                    }
                                }
                            )
                        }
                    }
                }
                
                // Voice Selection Button
                OutlinedButton(
                    onClick = { showVoiceDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Select Voice",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voice",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Voice Selection Dialog
            if (showVoiceDialog) {
                VoiceSelectionDialog(
                    ttsService = ttsService,
                    onDismiss = { showVoiceDialog = false }
                )
            }
            
            // Engine Initialization Dialog
            if (showInitDialog) {
                AlertDialog(
                    onDismissRequest = { 
                        // Allow dismissal at any time
                        showInitDialog = false
                        initError = null
                        isInitializing = false
                    },
                    title = {
                        Text(
                            text = if (isInitializing) "Initializing Engine..." else if (initError != null) "Initialization Failed" else "Success"
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (isInitializing) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text("Please wait while the engine initializes...")
                                }
                                Text(
                                    text = "This may take several minutes on first run...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else if (initError != null) {
                                Text(
                                    text = "Failed to initialize the TTS engine:\n\n$initError\n\nPlease test the engine in TTS Engine Manager settings.",
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text("Engine initialized successfully!")
                            }
                        }
                    },
                    confirmButton = {
                        // Only show button on error
                        if (!isInitializing && initError != null) {
                            TextButton(
                                onClick = {
                                    showInitDialog = false
                                    initError = null
                                    // TODO: Navigate to TTS Engine Manager
                                }
                            ) {
                                Text("Open TTS Manager")
                            }
                        }
                    },
                    dismissButton = {
                        // Always show cancel button
                        TextButton(
                            onClick = {
                                showInitDialog = false
                                initError = null
                                isInitializing = false
                            }
                        ) {
                            Text(if (isInitializing) "Cancel" else "Close")
                        }
                    }
                )
            }
        }
    }
}
