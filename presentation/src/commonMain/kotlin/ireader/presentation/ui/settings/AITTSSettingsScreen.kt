package ireader.presentation.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.services.tts.AITTSProvider
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.settings.viewmodels.AITTSSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AITTSSettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: AITTSSettingsViewModel
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TTS Engine Manager") },
                navigationIcon = {
                    AppIconButton(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBackPressed
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sherpa TTS App Recommendation (Android)
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Recommended: Sherpa TTS App",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Text(
                            text = "For more powerful and natural-sounding voices on Android, install the Sherpa TTS app from the Play Store or F-Droid.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Text(
                            text = "✓ High-quality neural voices\n✓ Works offline\n✓ Multiple languages\n✓ Integrates with Android TTS",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Once installed, go to Android Settings → Accessibility → Text-to-speech → Preferred engine and select Sherpa TTS.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            

            // Coqui TTS (gTTS) - Your Custom Space
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.useCoquiTTS) 
                            MaterialTheme.colorScheme.secondaryContainer 
                        else 
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cloud,
                                        contentDescription = null,
                                        tint = if (state.useCoquiTTS) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Coqui TTS (Your Space)",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Text(
                                    text = "Your custom TTS from Hugging Face",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.useCoquiTTS,
                                onCheckedChange = { viewModel.setUseCoquiTTS(it) }
                            )
                        }
                        
                        if (state.useCoquiTTS) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            // Space URL
                            Text(
                                text = "Space URL",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            OutlinedTextField(
                                value = state.coquiSpaceUrl,
                                onValueChange = { viewModel.setCoquiSpaceUrl(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("https://your-username-tts.hf.space") },
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Speed control
                            Text(
                                text = "Speed: ${String.format("%.1f", state.coquiSpeed)}x",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Slider(
                                value = state.coquiSpeed,
                                onValueChange = { viewModel.setCoquiSpeed(it) },
                                valueRange = 0.5f..2.0f,
                                steps = 15
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Test button
                            Button(
                                onClick = { viewModel.testCoquiTTS() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Test Coqui TTS")
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "✓ Your custom Space • ✓ Free forever • ✓ Good quality",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            if (!state.isCoquiAvailable) {
                                Text(
                                    text = "⚠️ Space may not be available. Check URL and internet connection.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
            
            // Native TTS Info
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Native Android TTS",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "IReader uses your device's built-in Text-to-Speech engine. You can configure voices in Android Settings → Accessibility → Text-to-speech.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✓ System integration • ✓ Multiple engines supported • ✓ No downloads needed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Piper TTS Voices Section
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Piper TTS Voices",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        
                        Text(
                            text = "High-quality offline neural voices. Download and use with Sherpa TTS app.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Show available voices count
                        val availableVoices = ireader.domain.catalogs.PiperVoiceCatalog.getAllVoices()
                        val languages = ireader.domain.catalogs.PiperVoiceCatalog.getSupportedLanguages()
                        
                        Text(
                            text = "${availableVoices.size} voices available in ${languages.size} languages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Voice list header
            item {
                Text(
                    text = "Available Voices",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Voice list
            val voices = ireader.domain.catalogs.PiperVoiceCatalog.getAllVoices()
            items(voices) { voice ->
                VoiceCard(
                    voice = voice,
                    isSelected = state.selectedVoiceId == voice.id,
                    isDownloading = state.downloadingVoice == voice.id,
                    isDownloaded = state.downloadedVoices.contains(voice.id),
                    downloadProgress = if (state.downloadingVoice == voice.id) state.downloadProgress else 0,
                    onSelect = { viewModel.selectVoice(voice.id) },
                    onPreview = { viewModel.previewVoice(voice.id) },
                    onDownload = { viewModel.downloadVoice(voice) },
                    onDelete = { viewModel.deleteVoice(voice.id) }
                )
            }
            
            // Error message
            state.error?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
    
}



@Composable
private fun VoiceCard(
    voice: ireader.domain.models.tts.VoiceModel,
    isSelected: Boolean,
    isDownloading: Boolean,
    isDownloaded: Boolean,
    downloadProgress: Int,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = voice.name,
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (isDownloaded) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Downloaded",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "${voice.locale} • ${voice.gender.name} • ${voice.quality.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (voice.modelSize > 0) {
                        Text(
                            text = "Size: ${voice.modelSize / 1_000_000}MB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isDownloaded) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, "Delete")
                        }
                        IconButton(onClick = onPreview) {
                            Icon(Icons.Default.PlayArrow, "Preview")
                        }
                    } else {
                        IconButton(onClick = onDownload, enabled = !isDownloading) {
                            Icon(Icons.Default.Download, "Download")
                        }
                    }
                    
                    RadioButton(
                        selected = isSelected,
                        onClick = onSelect,
                        enabled = isDownloaded
                    )
                }
            }
            
            if (isDownloading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = downloadProgress / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Downloading... $downloadProgress%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


