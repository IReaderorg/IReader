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
            // Enable AI TTS Toggle
            item {
                Card {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Piper TTS",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "High-quality offline AI voices (Free)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.useAITTS,
                            onCheckedChange = { viewModel.setUseAITTS(it) }
                        )
                    }
                }
            }
            
            // Provider Info
            if (state.useAITTS) {
                item {
                    Text(
                        text = "About Piper TTS",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                item {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Piper TTS",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "High-quality offline neural voices",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✓ Works offline • ✓ No API keys • ✓ Premium quality",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            if (state.downloadedVoices.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Downloaded: ${state.downloadedVoices.size} voices (${state.totalDownloadedSize / 1_000_000}MB)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Voice Selection
                if (state.availableVoices.isNotEmpty()) {
                    item {
                        Text(
                            text = "Available Voices",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(state.availableVoices) { voice ->
                        VoiceCard(
                            voice = voice,
                            isSelected = state.selectedVoiceId == voice.id,
                            isDownloading = state.downloadingVoice == voice.id,
                            isDownloaded = state.downloadedVoices.contains(voice.id),
                            downloadProgress = state.downloadProgress,
                            onSelect = { viewModel.selectVoice(voice.id) },
                            onPreview = { viewModel.previewVoice(voice.id) },
                            onDownload = { viewModel.downloadVoice(voice) },
                            onDelete = { viewModel.deleteVoice(voice.id) }
                        )
                    }
                }
                
                // Load Voices Button
                item {
                    Button(
                        onClick = { viewModel.loadVoices() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Load Voices")
                    }
                }
            }
            
            // Error Message
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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


