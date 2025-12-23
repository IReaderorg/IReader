package ireader.presentation.ui.book.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.TrackStatus
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

/**
 * Dialog for selecting tracking status
 */
@Composable
fun TrackingStatusDialog(
    currentStatus: String?,
    onStatusSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val statuses = TrackStatus.values().toList()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Status", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(statuses) { status ->
                    val isSelected = currentStatus == status.name
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable {
                            onStatusSelected(status.name)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getStatusIcon(status),
                                contentDescription = null,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Text(
                                text = getStatusDisplayName(status),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}

/**
 * Dialog for editing tracking progress (chapters read)
 */
@Composable
fun TrackingProgressDialog(
    currentProgress: Int?,
    totalChapters: Int?,
    onProgressUpdated: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var progressText by remember { mutableStateOf(currentProgress?.toString() ?: "0") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Progress", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = progressText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            progressText = newValue
                        }
                    },
                    label = { Text("Chapters Read") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = if (totalChapters != null && totalChapters > 0) {
                        { Text("Total: $totalChapters chapters") }
                    } else null
                )
                
                // Quick buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val current = progressText.toIntOrNull() ?: 0
                            if (current > 0) progressText = (current - 1).toString()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("-1")
                    }
                    OutlinedButton(
                        onClick = {
                            val current = progressText.toIntOrNull() ?: 0
                            progressText = (current + 1).toString()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+1")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val progress = progressText.toIntOrNull() ?: 0
                    onProgressUpdated(progress)
                    onDismiss()
                }
            ) {
                Text(localizeHelper.localize(Res.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}

/**
 * Dialog for editing tracking score
 */
@Composable
fun TrackingScoreDialog(
    currentScore: Float?,
    onScoreUpdated: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var scoreValue by remember { mutableStateOf(currentScore ?: 0f) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Score", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Score display
                Text(
                    text = if (scoreValue > 0) "%.1f".format(scoreValue) else "-",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "out of 10",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Slider
                Slider(
                    value = scoreValue,
                    onValueChange = { scoreValue = it },
                    valueRange = 0f..10f,
                    steps = 19, // 0.5 increments
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Quick score buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(0f, 5f, 7f, 8f, 9f, 10f).forEach { score ->
                        FilterChip(
                            selected = scoreValue == score,
                            onClick = { scoreValue = score },
                            label = { Text(if (score == 0f) "-" else score.toInt().toString()) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onScoreUpdated(scoreValue)
                    onDismiss()
                }
            ) {
                Text(localizeHelper.localize(Res.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}

private fun getStatusIcon(status: TrackStatus) = when (status) {
    TrackStatus.Reading -> Icons.Default.MenuBook
    TrackStatus.Completed -> Icons.Default.CheckCircle
    TrackStatus.OnHold -> Icons.Default.Pause
    TrackStatus.Dropped -> Icons.Default.Cancel
    TrackStatus.Planned -> Icons.Default.Schedule
    TrackStatus.Repeating -> Icons.Default.Replay
}

private fun getStatusDisplayName(status: TrackStatus) = when (status) {
    TrackStatus.Reading -> "Reading"
    TrackStatus.Completed -> "Completed"
    TrackStatus.OnHold -> "On Hold"
    TrackStatus.Dropped -> "Dropped"
    TrackStatus.Planned -> "Plan to Read"
    TrackStatus.Repeating -> "Re-reading"
}
