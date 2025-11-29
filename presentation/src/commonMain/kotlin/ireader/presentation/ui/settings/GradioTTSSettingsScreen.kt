package ireader.presentation.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.services.tts_service.GradioTTSConfig
import ireader.domain.services.tts_service.GradioTTSPresets
import ireader.presentation.ui.component.reusable_composable.AppIconButton

/**
 * Gradio TTS Settings Screen
 * Allows users to select from preset TTS engines or create custom configurations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradioTTSSettingsScreen(
    useGradioTTS: Boolean,
    activeConfigId: String?,
    globalSpeed: Float,
    presetConfigs: List<GradioTTSConfig>,
    customConfigs: List<GradioTTSConfig>,
    isTesting: Boolean,
    onBackPressed: () -> Unit,
    onUseGradioTTSChange: (Boolean) -> Unit,
    onSelectConfig: (String) -> Unit,
    onGlobalSpeedChange: (Float) -> Unit,
    onTestConfig: (String) -> Unit,
    onEditConfig: (GradioTTSConfig) -> Unit,
    onDuplicateConfig: (GradioTTSConfig) -> Unit,
    onDeleteConfig: (String) -> Unit,
    onCreateCustomConfig: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gradio TTS Engines") },
                navigationIcon = {
                    AppIconButton(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBackPressed
                    )
                },
                actions = {
                    IconButton(onClick = onCreateCustomConfig) {
                        Icon(Icons.Default.Add, contentDescription = "Add Custom")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Master toggle
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (useGradioTTS)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    )
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
                                text = "Enable Gradio TTS",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Use online TTS engines from Hugging Face Spaces",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = useGradioTTS,
                            onCheckedChange = onUseGradioTTSChange
                        )
                    }
                }
            }
            
            // Global speed control
            if (useGradioTTS) {
                item {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Global Speed: ${String.format("%.1f", globalSpeed)}x",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Slider(
                                value = globalSpeed,
                                onValueChange = onGlobalSpeedChange,
                                valueRange = 0.5f..2.0f,
                                steps = 15
                            )
                        }
                    }
                }
            }
            
            // Preset configurations section
            item {
                Text(
                    text = "Preset Engines",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            items(presetConfigs) { config ->
                GradioConfigCard(
                    config = config,
                    isActive = activeConfigId == config.id,
                    onSelect = { onSelectConfig(config.id) },
                    onTest = { onTestConfig(config.id) },
                    onEdit = { onEditConfig(config) },
                    onDuplicate = { onDuplicateConfig(config) },
                    onDelete = null,
                    isTesting = isTesting && activeConfigId == config.id
                )
            }
            
            // Custom configurations section
            if (customConfigs.isNotEmpty()) {
                item {
                    Text(
                        text = "Custom Engines",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                
                items(customConfigs) { config ->
                    GradioConfigCard(
                        config = config,
                        isActive = activeConfigId == config.id,
                        onSelect = { onSelectConfig(config.id) },
                        onTest = { onTestConfig(config.id) },
                        onEdit = { onEditConfig(config) },
                        onDuplicate = { onDuplicateConfig(config) },
                        onDelete = { onDeleteConfig(config.id) },
                        isTesting = isTesting && activeConfigId == config.id
                    )
                }
            }
            
            // Add custom button
            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCreateCustomConfig() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Custom TTS Engine",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Info card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "About Gradio TTS",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Text(
                            text = "Gradio TTS engines connect to Hugging Face Spaces to generate speech. They require an internet connection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GradioConfigCard(
    config: GradioTTSConfig,
    isActive: Boolean,
    onSelect: () -> Unit,
    onTest: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: (() -> Unit)?,
    isTesting: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                        RadioButton(selected = isActive, onClick = onSelect)
                        Text(
                            text = config.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (config.isCustom) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "Custom",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                    if (config.description.isNotEmpty()) {
                        Text(
                            text = config.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 40.dp)
                        )
                    }
                }
            }
            
            Text(
                text = config.spaceUrl.ifEmpty { "No URL configured" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onTest, enabled = !isTesting) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test")
                }
                
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                
                TextButton(onClick = onDuplicate) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy")
                }
                
                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
