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
    onDismiss: () -> Unit,
    onNavigateToTTSManager: () -> Unit
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
                    
                    // Coqui TTS (Online) - Navigate to TTS Manager for configuration
                    EngineCard(
                        name = "Coqui TTS (Online)",
                        description = "High-quality neural TTS via HTTP server",
                        isAvailable = true, // Always available if server is configured
                        isCurrentEngine = false,
                        onSelect = {
                            onNavigateToTTSManager()
                            onDismiss()
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


/**
 * Desktop implementation of TTS Voice Selection Screen
 * 
 * Shows available Piper/Kokoro/Maya voices based on current engine
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun TTSVoiceSelectionScreen(
    isDesktop: Boolean,
    onDismiss: () -> Unit
) {
    val ttsService: DesktopTTSService = koinInject()
    val scope = rememberCoroutineScope()
    
    // Voice state
    var availableVoices by remember { mutableStateOf<List<VoiceInfo>>(emptyList()) }
    var selectedVoice by remember { mutableStateOf<String?>(null) }
    var currentEngine by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load voices based on current engine
    LaunchedEffect(Unit) {
        currentEngine = ttsService.getCurrentEngine().name
        selectedVoice = ttsService.state.selectedVoiceModel?.id
        
        // Get available voices based on engine
        availableVoices = when (currentEngine) {
            "PIPER" -> {
                ttsService.state.availableVoiceModels
                    .filter { it.isDownloaded }
                    .map { VoiceInfo(it.id, it.name, it.language, true) }
            }
            "KOKORO" -> {
                // Kokoro has predefined voices
                listOf(
                    VoiceInfo("af_bella", "Bella (Female)", "en-US", true),
                    VoiceInfo("af_nicole", "Nicole (Female)", "en-US", true),
                    VoiceInfo("af_sarah", "Sarah (Female)", "en-US", true),
                    VoiceInfo("af_sky", "Sky (Female)", "en-US", true),
                    VoiceInfo("am_adam", "Adam (Male)", "en-US", true),
                    VoiceInfo("am_michael", "Michael (Male)", "en-US", true),
                    VoiceInfo("bf_emma", "Emma (Female)", "en-GB", true),
                    VoiceInfo("bm_george", "George (Male)", "en-GB", true)
                )
            }
            "MAYA" -> {
                // Maya supports multiple languages
                listOf(
                    VoiceInfo("en", "English", "en", true),
                    VoiceInfo("es", "Spanish", "es", true),
                    VoiceInfo("fr", "French", "fr", true),
                    VoiceInfo("de", "German", "de", true),
                    VoiceInfo("it", "Italian", "it", true),
                    VoiceInfo("pt", "Portuguese", "pt", true),
                    VoiceInfo("ru", "Russian", "ru", true),
                    VoiceInfo("zh", "Chinese", "zh", true),
                    VoiceInfo("ja", "Japanese", "ja", true),
                    VoiceInfo("ko", "Korean", "ko", true)
                )
            }
            else -> emptyList()
        }
        isLoading = false
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .fillMaxHeight(0.8f),
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
                    Column {
                        Text(
                            text = "Voice Selection",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Engine: $currentEngine",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (availableVoices.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No voices available",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Please install voices from the TTS Engine Manager",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Voice list
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableVoices.forEach { voice ->
                            VoiceCard(
                                voice = voice,
                                isSelected = voice.id == selectedVoice,
                                onSelect = {
                                    scope.launch {
                                        when (currentEngine) {
                                            "PIPER" -> {
                                                ttsService.selectVoiceModel(voice.id)
                                            }
                                            // For Kokoro and Maya, voice selection is handled differently
                                            // They use the voice ID directly during synthesis
                                        }
                                        selectedVoice = voice.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class VoiceInfo(
    val id: String,
    val name: String,
    val language: String,
    val isAvailable: Boolean
)

@Composable
private fun VoiceCard(
    voice: VoiceInfo,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    OutlinedCard(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
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
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = voice.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = voice.language,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                AssistChip(
                    onClick = {},
                    label = { Text("Selected") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}
