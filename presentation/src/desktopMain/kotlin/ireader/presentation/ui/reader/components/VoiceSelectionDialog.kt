package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ireader.core.log.Log
import ireader.domain.services.tts_service.DesktopTTSService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Voice Selection Dialog
 * Shows available voices based on the current TTS engine
 * Uses a proper Dialog window for better desktop experience
 */
@Composable
fun VoiceSelectionDialog(
    ttsService: DesktopTTSService,
    onDismiss: () -> Unit
) {
    val currentEngine = ttsService.getCurrentEngine()
    val scope = rememberCoroutineScope()
    val voicePreferences: ireader.domain.preferences.VoicePreferences = koinInject()
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(700.dp)
                .heightIn(max = 600.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentEngine) {
                            DesktopTTSService.TTSEngine.PIPER -> "Select Piper Voice"
                            DesktopTTSService.TTSEngine.KOKORO -> "Select Kokoro Voice"
                            DesktopTTSService.TTSEngine.MAYA -> "Select Maya Language"
                            DesktopTTSService.TTSEngine.SIMULATION -> "Simulation Mode"
                        },
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                Divider()
                
                // Content - Scrollable
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val scrollState = rememberScrollState()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (currentEngine) {
                            DesktopTTSService.TTSEngine.PIPER -> {
                                // Show VoiceModelManagementPanel for Piper
                                VoiceModelManagementPanel(
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            
                            DesktopTTSService.TTSEngine.KOKORO -> {
                                // Show Kokoro voices
                                Text(
                                    text = "Available Kokoro voices:",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                val voices = ttsService.kokoroAdapter.getAvailableVoices()
                                
                                voices.forEach { voice ->
                                    OutlinedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    // Save selected Kokoro voice to preferences
                                                    voicePreferences.setSelectedKokoroVoice(voice.id)
                                                    
                                                    // Apply to TTS service
                                                    ttsService.kokoroAdapter.setVoice(voice.id)
                                                    
                                                    Log.info { "Kokoro voice selected: ${voice.name}" }
                                                } catch (e: Exception) {
                                                    Log.error("Failed to set Kokoro voice", e)
                                                }
                                            }
                                            onDismiss()
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = voice.name,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                                Text(
                                                    text = "${voice.accent} - ${voice.gender}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = voice.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            DesktopTTSService.TTSEngine.MAYA -> {
                                // Show Maya languages
                                Text(
                                    text = "Available languages:",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                val languages = listOf(
                                    "English" to "en",
                                    "Spanish" to "es",
                                    "French" to "fr",
                                    "German" to "de",
                                    "Italian" to "it",
                                    "Portuguese" to "pt",
                                    "Polish" to "pl",
                                    "Turkish" to "tr",
                                    "Russian" to "ru",
                                    "Dutch" to "nl",
                                    "Czech" to "cs",
                                    "Arabic" to "ar",
                                    "Chinese" to "zh",
                                    "Japanese" to "ja",
                                    "Korean" to "ko",
                                    "Hindi" to "hi"
                                )
                                
                                languages.forEach { (name, code) ->
                                    OutlinedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    // Save selected Maya language to preferences
                                                    voicePreferences.setSelectedMayaLanguage(code)
                                                    
                                                    // Apply to TTS service
                                                    ttsService.mayaAdapter.setLanguage(code)
                                                    
                                                    Log.info { "Maya language selected: $name ($code)" }
                                                } catch (e: Exception) {
                                                    Log.error("Failed to set Maya language", e)
                                                }
                                            }
                                            onDismiss()
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = name,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                            }
                            
                            DesktopTTSService.TTSEngine.SIMULATION -> {
                                Text(
                                    text = "Simulation mode does not use real voices.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
