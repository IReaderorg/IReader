package ireader.presentation.ui.reader.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.services.tts_service.piper.VoiceModel
import kotlin.math.roundToInt
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Composable that displays a list of available voice models with metadata
 * 
 * Displays:
 * - List of available voice models
 * - Model metadata (language, quality, size)
 * - Downloaded vs available status indicator
 * 
 * Requirements: 2.1
 */
@Composable
fun VoiceModelSelector(
    models: List<VoiceModel>,
    selectedModelId: String?,
    onModelSelected: (VoiceModel) -> Unit,
    onDownloadModel: (VoiceModel) -> Unit,
    onDeleteModel: (VoiceModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Voice Models",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (models.isEmpty()) {
                Text(
                    text = "No voice models available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(models) { model ->
                        VoiceModelItem(
                            model = model,
                            isSelected = model.id == selectedModelId,
                            onModelSelected = onModelSelected,
                            onDownloadModel = onDownloadModel,
                            onDeleteModel = onDeleteModel
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual voice model item displaying metadata and status
 */
@Composable
private fun VoiceModelItem(
    model: VoiceModel,
    isSelected: Boolean,
    onModelSelected: (VoiceModel) -> Unit,
    onDownloadModel: (VoiceModel) -> Unit,
    onDeleteModel: (VoiceModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = model.isDownloaded) { 
                if (model.isDownloaded) {
                    onModelSelected(model)
                }
            },
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
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Model info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimary
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (model.isDownloaded) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = localizeHelper.localize(Res.string.downloaded),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Metadata row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Language
                    MetadataChip(
                        text = model.language,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    
                    // Quality
                    MetadataChip(
                        text = model.quality.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = when (model.quality) {
                            VoiceModel.Quality.HIGH -> MaterialTheme.colorScheme.tertiary
                            VoiceModel.Quality.MEDIUM -> MaterialTheme.colorScheme.secondary
                            VoiceModel.Quality.LOW -> MaterialTheme.colorScheme.outline
                        }
                    )
                    
                    // Gender
                    MetadataChip(
                        text = model.gender.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                // Size
                Text(
                    text = formatFileSize(model.sizeBytes),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!model.isDownloaded) {
                    IconButton(
                        onClick = { onDownloadModel(model) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = localizeHelper.localize(Res.string.download),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    // Delete button for downloaded models
                    IconButton(
                        onClick = { onDeleteModel(model) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = localizeHelper.localize(Res.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Small chip for displaying metadata
 */
@Composable
private fun MetadataChip(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            // Use a contrasting color that works on both light and dark backgrounds
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Format file size in bytes to human-readable format
 */
private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    
    return when {
        gb >= 1.0 -> "${gb.roundToInt()} GB"
        mb >= 1.0 -> "${mb.roundToInt()} MB"
        kb >= 1.0 -> "${kb.roundToInt()} KB"
        else -> "$bytes B"
    }
}
