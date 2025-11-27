package ireader.presentation.ui.home.tts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ireader.domain.services.tts_service.DesktopTTSService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Desktop implementation of TTS Engine Settings Screen
 * 
 * Shows the TTS Engine Manager with options to:
 * - View installed engines (Piper, Kokoro, Maya)
 * - Install/uninstall engines
 * - Test engines
 * - Configure Coqui TTS server
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun TTSEngineSettingsScreen(
    isDesktop: Boolean,
    onDismiss: () -> Unit
) {
    val ttsService: DesktopTTSService = koinInject()
    val scope = rememberCoroutineScope()
    
    // Engine status
    var piperAvailable by remember { mutableStateOf(false) }
    var kokoroAvailable by remember { mutableStateOf(false) }
    var mayaAvailable by remember { mutableStateOf(false) }
    var currentEngine by remember { mutableStateOf("") }
    
    // Check engine status
    LaunchedEffect(Unit) {
        piperAvailable = ttsService.synthesizer.isInitialized()
        kokoroAvailable = ttsService.kokoroAvailable
        mayaAvailable = ttsService.mayaAvailable
        currentEngine = ttsService.getCurrentEngine().name
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.85f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TTS Engine Manager",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                // Current engine info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Current Engine",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = currentEngine,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Engine list
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Piper TTS
                    EngineCard(
                        name = "Piper TTS",
                        description = "High-performance neural TTS with 30+ voices",
                        isAvailable = piperAvailable,
                        isCurrentEngine = currentEngine == "PIPER",
                        onSelect = {
                            scope.launch {
                                ttsService.setEngine(ireader.domain.services.tts_service.DesktopTTSService.TTSEngine.PIPER)
                                currentEngine = "PIPER"
                            }
                        }
                    )
                    
                    // Kokoro TTS
                    EngineCard(
                        name = "Kokoro TTS",
                        description = "Premium neural TTS with natural-sounding voices",
                        isAvailable = kokoroAvailable,
                        isCurrentEngine = currentEngine == "KOKORO",
                        onSelect = {
                            scope.launch {
                                ttsService.setEngine(ireader.domain.services.tts_service.DesktopTTSService.TTSEngine.KOKORO)
                                currentEngine = "KOKORO"
                            }
                        }
                    )
                    
                    // Maya TTS
                    EngineCard(
                        name = "Maya TTS",
                        description = "Multilingual neural TTS from Maya Research",
                        isAvailable = mayaAvailable,
                        isCurrentEngine = currentEngine == "MAYA",
                        onSelect = {
                            scope.launch {
                                ttsService.setEngine(ireader.domain.services.tts_service.DesktopTTSService.TTSEngine.MAYA)
                                currentEngine = "MAYA"
                            }
                        }
                    )
                    
                    // Coqui TTS (Online)
                    EngineCard(
                        name = "Coqui TTS (Online)",
                        description = "High-quality neural TTS via HTTP server",
                        isAvailable = true, // Always available if server is configured
                        isCurrentEngine = false,
                        onSelect = {
                            // TODO: Configure Coqui TTS server URL
                        }
                    )
                }
                
                // Help text
                Text(
                    text = "Note: Some engines require installation. Use the full TTS Manager in Settings for installation options.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun EngineCard(
    name: String,
    description: String,
    isAvailable: Boolean,
    isCurrentEngine: Boolean,
    onSelect: () -> Unit
) {
    OutlinedCard(
        onClick = { if (isAvailable) onSelect() },
        modifier = Modifier.fillMaxWidth(),
        enabled = isAvailable,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isCurrentEngine) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            Icon(
                imageVector = when {
                    isCurrentEngine -> Icons.Default.CheckCircle
                    isAvailable -> Icons.Default.RadioButtonUnchecked
                    else -> Icons.Default.Cancel
                },
                contentDescription = null,
                tint = when {
                    isCurrentEngine -> MaterialTheme.colorScheme.primary
                    isAvailable -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.error
                }
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isAvailable) {
                    Text(
                        text = "Not installed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            if (isCurrentEngine) {
                AssistChip(
                    onClick = {},
                    label = { Text("Active") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}
