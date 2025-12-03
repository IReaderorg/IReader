package ireader.presentation.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.models.tts.VoiceGender
import ireader.domain.models.tts.VoiceModel
import ireader.domain.models.tts.VoiceQuality
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Voice card component displaying voice metadata and actions
 * Requirements: 4.1, 10.1
 */
@Composable
fun VoiceCard(
    voice: VoiceModel,
    isSelected: Boolean,
    isDownloaded: Boolean,
    downloadProgress: Float?,
    onSelect: () -> Unit,
    onDownload: () -> Unit,
    onPreview: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row with name and quality badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = voice.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${voice.locale} • ${formatGender(voice.gender)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                QualityBadge(quality = voice.quality)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Description
            Text(
                text = voice.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer row with size and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Size info
                Text(
                    text = formatFileSize(voice.modelSize),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Preview button (always visible if downloaded)
                    if (isDownloaded) {
                        IconButton(
                            onClick = onPreview,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = localizeHelper.localize(Res.string.preview_voice),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Download progress or action button
                    when {
                        downloadProgress != null -> {
                            Box(
                                modifier = Modifier.size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = downloadProgress,
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp
                                )
                            }
                        }
                        isDownloaded -> {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = localizeHelper.localize(Res.string.delete_voice),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        else -> {
                            FilledTonalButton(
                                onClick = onDownload,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(localizeHelper.localize(Res.string.download))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Quality badge component
 */
@Composable
fun QualityBadge(
    quality: VoiceQuality,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (quality) {
        VoiceQuality.LOW -> "Low" to MaterialTheme.colorScheme.surfaceVariant
        VoiceQuality.MEDIUM -> "Medium" to MaterialTheme.colorScheme.secondaryContainer
        VoiceQuality.HIGH -> "High" to MaterialTheme.colorScheme.primaryContainer
        VoiceQuality.PREMIUM -> "Premium" to MaterialTheme.colorScheme.tertiaryContainer
    }
    
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = color
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Format file size in human-readable format
 */
fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${ireader.presentation.ui.core.utils.toDecimalString(bytes / (1024.0 * 1024.0 * 1024.0), 1)} GB"
    }
}

/**
 * Format gender for display
 */
private fun formatGender(gender: VoiceGender): String {
    return when (gender) {
        VoiceGender.MALE -> "Male"
        VoiceGender.FEMALE -> "Female"
        VoiceGender.NEUTRAL -> "Neutral"
    }
}
