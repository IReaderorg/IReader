package ireader.presentation.ui.settings.translation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Compact Gemini model selection with refresh capability
 * Optimized for mobile screens
 */
@Composable
fun GeminiModelSelector(
    models: List<Pair<String, String>>,
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    onRefreshModels: () -> Unit,
    isRefreshing: Boolean,
    refreshMessage: String?,
    apiKeySet: Boolean,
    modifier: Modifier = Modifier
) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with refresh button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedModel.isNotBlank()) "Model: ${getModelDisplayName(selectedModel, models)}" else "Select Model",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                FilledTonalButton(
                    onClick = onRefreshModels,
                    enabled = !isRefreshing && apiKeySet,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refresh", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Status message
            AnimatedVisibility(
                visible = refreshMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                refreshMessage?.let { message ->
                    val isSuccess = message.startsWith("Success")
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSuccess)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Model selection
            if (models.isEmpty()) {
                // Empty state
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (!apiKeySet) "Enter API key, then refresh" else "Click Refresh to load models",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp),
                        maxLines = 1
                    )
                }
            } else {
                // Model chips in horizontal scroll
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(models, key = { it.first }) { (modelId, displayName) ->
                        FilterChip(
                            selected = selectedModel == modelId,
                            onClick = { onModelSelected(modelId) },
                            label = {
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            },
                            leadingIcon = if (selectedModel == modelId) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }

            // Help text
            Text(
                text = "Auto-fallback if quota exceeded",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = 1
            )
        }
    }
}

private fun getModelDisplayName(modelId: String, models: List<Pair<String, String>>): String {
    return models.find { it.first == modelId }?.second ?: modelId.substringAfterLast("-")
}
