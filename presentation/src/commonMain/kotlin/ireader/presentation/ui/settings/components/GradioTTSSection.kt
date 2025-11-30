package ireader.presentation.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.services.tts_service.GradioParam
import ireader.domain.services.tts_service.GradioParamType
import ireader.domain.services.tts_service.GradioTTSConfig
import ireader.domain.services.tts_service.GradioTTSPresets
import ireader.presentation.ui.core.modifier.supportDesktopHorizontalLazyListScroll
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Gradio TTS Section - Common UI component for both Android and Desktop
 * Displays and manages Gradio TTS configurations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradioTTSSection(
    useGradioTTS: Boolean,
    onUseGradioTTSChange: (Boolean) -> Unit,
    configs: List<GradioTTSConfig>,
    activeConfigId: String?,
    onSelectConfig: (String) -> Unit,
    onTestConfig: (String) -> Unit,
    onEditConfig: (GradioTTSConfig) -> Unit,
    onDeleteConfig: (String) -> Unit,
    onAddCustomConfig: () -> Unit,
    globalSpeed: Float,
    onGlobalSpeedChange: (Float) -> Unit,
    isTesting: Boolean,
    testingConfigId: String?,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Master toggle card
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = if (useGradioTTS)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.gradio_tts_online),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        text = localizeHelper.localize(Res.string.use_online_tts_engines_from_hugging_face_spaces),
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
        
        // Show configuration options when enabled
        if (useGradioTTS) {
            // Global speed control
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Global Speed: ${String.format("%.1f", globalSpeed)}x",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Slider(
                        value = globalSpeed,
                        onValueChange = onGlobalSpeedChange,
                        valueRange = 0.5f..2.0f,
                        steps = 15
                    )
                }
            }
            
            // Preset engines section
            Text(
                text = localizeHelper.localize(Res.string.available_engines),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            // Show configs
            configs.forEach { config ->
                GradioConfigCard(
                    config = config,
                    isActive = activeConfigId == config.id,
                    onSelect = { onSelectConfig(config.id) },
                    onTest = { onTestConfig(config.id) },
                    onEdit = { onEditConfig(config) },
                    onDelete = if (config.isCustom) {{ onDeleteConfig(config.id) }} else null,
                    isTesting = isTesting && testingConfigId == config.id
                )
            }
            
            // Add custom button
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddCustomConfig() }
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
                        text = localizeHelper.localize(Res.string.add_custom_tts_engine),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.about_gradio_tts),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Text(
                        text = localizeHelper.localize(Res.string.gradio_tts_connects_to_hugging),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    onDelete: (() -> Unit)?,
    isTesting: Boolean
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = isActive,
                        onClick = onSelect
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = config.name,
                                style = MaterialTheme.typography.titleSmall
                            )
                            if (config.isCustom) {
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                    Text(
                                        text = localizeHelper.localize(Res.string.custom),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
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
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            
            // Action buttons row with horizontal scroll support for desktop
            val actionRowState = rememberLazyListState()
            val scope = rememberCoroutineScope()
            
            LazyRow(
                state = actionRowState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .supportDesktopHorizontalLazyListScroll(actionRowState, scope),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    TextButton(
                        onClick = onTest,
                        enabled = !isTesting,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test", style = MaterialTheme.typography.labelMedium)
                    }
                }
                
                item {
                    TextButton(
                        onClick = onEdit,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", style = MaterialTheme.typography.labelMedium)
                    }
                }
                
                if (onDelete != null) {
                    item {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
