package ireader.presentation.ui.reader.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.services.tts.VoiceConfiguration
import ireader.domain.services.tts.VoiceFilter
import ireader.domain.services.tts.VoiceSelectionState
import ireader.domain.services.tts.VoiceSource
import ireader.domain.services.tts.VoiceSourceType
import ireader.domain.services.tts.VoiceWithSource
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Enhanced voice selector that supports both built-in and plugin voices
 * Requirements: 5.1, 5.2, 5.3
 */
@Composable
fun PluginVoiceSelector(
    state: VoiceSelectionState,
    onVoiceSelected: (String) -> Unit,
    onPreviewVoice: (String) -> Unit,
    onConfigurationChanged: (VoiceConfiguration) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = localizeHelper.localize(Res.string.voice_selection),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filter section
            VoiceFilterSection(
                state = state,
                onFilterChanged = { /* Update filter */ }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Voice list
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                val errorMessage = state.error ?: "Unknown error"
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                val filteredVoices = state.getFilteredVoices()
                
                if (filteredVoices.isEmpty()) {
                    Text(
                        text = localizeHelper.localize(Res.string.no_voices_available),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredVoices) { voiceWithSource ->
                            VoiceItem(
                                voiceWithSource = voiceWithSource,
                                isSelected = voiceWithSource.voice.id == state.selectedVoiceId,
                                isPreviewing = state.isPreviewing,
                                onSelect = { onVoiceSelected(voiceWithSource.voice.id) },
                                onPreview = { onPreviewVoice(voiceWithSource.voice.id) }
                            )
                        }
                    }
                }
            }
            
            // Configuration section
            if (state.selectedVoiceId != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                
                VoiceConfigurationSection(
                    configuration = state.configuration,
                    onConfigurationChanged = onConfigurationChanged
                )
            }
        }
    }
}

/**
 * Filter section for voice selection
 * Requirements: 5.1
 */
@Composable
private fun VoiceFilterSection(
    state: VoiceSelectionState,
    onFilterChanged: (VoiceFilter) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Source type filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.getAvailableSourceTypes().forEach { sourceType ->
                FilterChip(
                    selected = state.filter.sourceType == sourceType,
                    onClick = {
                        onFilterChanged(state.filter.copy(sourceType = sourceType))
                    },
                    label = {
                        Text(
                            when (sourceType) {
                                VoiceSourceType.ALL -> "All"
                                VoiceSourceType.BUILT_IN -> "Built-in"
                                VoiceSourceType.PLUGIN -> "Plugins"
                            }
                        )
                    }
                )
            }
        }
        
        // Language filter
        if (state.getAvailableLanguages().isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.filter.language == null,
                    onClick = {
                        onFilterChanged(state.filter.copy(language = null))
                    },
                    label = { Text(localizeHelper.localize(Res.string.all_languages)) }
                )
                
                // Show first few languages
                state.getAvailableLanguages().take(3).forEach { language ->
                    FilterChip(
                        selected = state.filter.language == language,
                        onClick = {
                            onFilterChanged(state.filter.copy(language = language))
                        },
                        label = { Text(language.uppercase()) }
                    )
                }
            }
        }
    }
}

/**
 * Individual voice item
 * Requirements: 5.1, 5.2, 5.3
 */
@Composable
private fun VoiceItem(
    voiceWithSource: VoiceWithSource,
    isSelected: Boolean,
    isPreviewing: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Voice name with source badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = voiceWithSource.voice.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // Source badge
                    when (val source = voiceWithSource.source) {
                        is VoiceSource.BuiltIn -> {
                            Surface(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = localizeHelper.localize(Res.string.built_in),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        is VoiceSource.Plugin -> {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Extension,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = source.pluginName,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Voice metadata
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = voiceWithSource.voice.locale,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = voiceWithSource.voice.gender.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = voiceWithSource.voice.quality.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Description
                Text(
                    text = voiceWithSource.voice.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            
            // Preview button
            IconButton(
                onClick = onPreview,
                enabled = !isPreviewing
            ) {
                Icon(
                    imageVector = if (isPreviewing) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPreviewing) "Stop preview" else "Preview voice",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Voice configuration section
 * Requirements: 5.3, 5.5
 */
@Composable
private fun VoiceConfigurationSection(
    configuration: VoiceConfiguration,
    onConfigurationChanged: (VoiceConfiguration) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = localizeHelper.localize(Res.string.voice_settings),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        
        // Speed slider
        ConfigurationSlider(
            label = localizeHelper.localize(Res.string.speed),
            value = configuration.speed,
            valueRange = VoiceConfiguration.MIN_SPEED..VoiceConfiguration.MAX_SPEED,
            onValueChange = { onConfigurationChanged(configuration.copy(speed = it)) }
        )
        
        // Pitch slider
        ConfigurationSlider(
            label = localizeHelper.localize(Res.string.pitch),
            value = configuration.pitch,
            valueRange = VoiceConfiguration.MIN_PITCH..VoiceConfiguration.MAX_PITCH,
            onValueChange = { onConfigurationChanged(configuration.copy(pitch = it)) }
        )
        
        // Volume slider
        ConfigurationSlider(
            label = localizeHelper.localize(Res.string.volume),
            value = configuration.volume,
            valueRange = VoiceConfiguration.MIN_VOLUME..VoiceConfiguration.MAX_VOLUME,
            onValueChange = { onConfigurationChanged(configuration.copy(volume = it)) }
        )
        
        // Streaming toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = localizeHelper.localize(Res.string.enable_streaming),
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = configuration.enableStreaming,
                onCheckedChange = {
                    onConfigurationChanged(configuration.copy(enableStreaming = it))
                }
            )
        }
    }
}

/**
 * Configuration slider component
 */
@Composable
private fun ConfigurationSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = ireader.presentation.ui.core.utils.formatDecimal(value.toDouble(), 2),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = 29 // For 0.05 increments
        )
    }
}
