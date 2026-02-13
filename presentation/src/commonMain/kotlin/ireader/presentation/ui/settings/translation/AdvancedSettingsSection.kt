package ireader.presentation.ui.settings.translation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.data.engines.ContentType
import ireader.domain.data.engines.ToneType
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Compact advanced AI translation settings
 * Optimized for mobile screens
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdvancedSettingsSection(
    contentType: Int,
    toneType: Int,
    preserveStyle: Boolean,
    customPrompt: String,
    onContentTypeChange: (Int) -> Unit,
    onToneTypeChange: (Int) -> Unit,
    onPreserveStyleChange: (Boolean) -> Unit,
    onCustomPromptChange: (String) -> Unit,
    isAiEngine: Boolean,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    if (!isAiEngine) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Content Type
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = localizeHelper.localize(Res.string.content_type),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ContentType.entries.forEachIndexed { index, type ->
                        FilterChip(
                            selected = contentType == index,
                            onClick = { onContentTypeChange(index) },
                            label = {
                                Text(
                                    text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Tone Type
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = localizeHelper.localize(Res.string.tone_type),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ToneType.entries.forEachIndexed { index, type ->
                        FilterChip(
                            selected = toneType == index,
                            onClick = { onToneTypeChange(index) },
                            label = {
                                Text(
                                    text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Preserve Style Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizeHelper.localize(Res.string.preserve_style),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = localizeHelper.localize(Res.string.keep_original_writing_style),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Switch(
                    checked = preserveStyle,
                    onCheckedChange = onPreserveStyleChange
                )
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            
            // Custom Prompt Text Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Custom Prompt",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Add custom instructions to append to translation prompts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = customPrompt,
                    onValueChange = onCustomPromptChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { 
                        Text(
                            "e.g., Use British English spelling, Keep honorifics in Japanese",
                            style = MaterialTheme.typography.bodySmall
                        ) 
                    },
                    minLines = 2,
                    maxLines = 4,
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
