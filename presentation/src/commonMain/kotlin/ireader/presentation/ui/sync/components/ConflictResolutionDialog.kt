package ireader.presentation.ui.sync.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ireader.domain.models.sync.ConflictResolutionStrategy
import ireader.domain.models.sync.DataConflict
import ireader.presentation.ui.component.components.IAlertDialog

/**
 * Dialog for resolving data conflicts during sync.
 * 
 * Displays conflict details and allows user to choose a resolution strategy.
 * Follows Material Design 3 dialog patterns with proper accessibility.
 * 
 * @param conflicts List of conflicts to resolve
 * @param onResolve Callback when user confirms resolution with selected strategy
 * @param onDismiss Callback when user dismisses the dialog
 * @param modifier Optional modifier for the dialog
 */
@Composable
fun ConflictResolutionDialog(
    conflicts: List<DataConflict>,
    onResolve: (ConflictResolutionStrategy) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStrategy by remember { mutableStateOf(ConflictResolutionStrategy.LATEST_TIMESTAMP) }
    
    IAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Resolve Conflicts")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Found ${conflicts.size} conflict${if (conflicts.size > 1) "s" else ""} during sync.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Show first conflict as example
                if (conflicts.isNotEmpty()) {
                    ConflictDetails(conflict = conflicts.first())
                    
                    if (conflicts.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "...and ${conflicts.size - 1} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Choose resolution strategy:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Resolution strategy options
                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    ResolutionStrategyOption(
                        strategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
                        label = "Use Latest",
                        description = "Use data with the most recent timestamp",
                        selected = selectedStrategy == ConflictResolutionStrategy.LATEST_TIMESTAMP,
                        onSelect = { selectedStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP }
                    )
                    
                    ResolutionStrategyOption(
                        strategy = ConflictResolutionStrategy.LOCAL_WINS,
                        label = "Use Local",
                        description = "Keep data from this device",
                        selected = selectedStrategy == ConflictResolutionStrategy.LOCAL_WINS,
                        onSelect = { selectedStrategy = ConflictResolutionStrategy.LOCAL_WINS }
                    )
                    
                    ResolutionStrategyOption(
                        strategy = ConflictResolutionStrategy.REMOTE_WINS,
                        label = "Use Remote",
                        description = "Use data from the other device",
                        selected = selectedStrategy == ConflictResolutionStrategy.REMOTE_WINS,
                        onSelect = { selectedStrategy = ConflictResolutionStrategy.REMOTE_WINS }
                    )
                    
                    ResolutionStrategyOption(
                        strategy = ConflictResolutionStrategy.MERGE,
                        label = "Merge",
                        description = "Attempt to merge compatible changes",
                        selected = selectedStrategy == ConflictResolutionStrategy.MERGE,
                        onSelect = { selectedStrategy = ConflictResolutionStrategy.MERGE }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onResolve(selectedStrategy) }) {
                Text("Resolve")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

/**
 * Displays details of a single conflict.
 */
@Composable
private fun ConflictDetails(
    conflict: DataConflict,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "Conflict Type: ${conflict.conflictType.name.replace("_", " ")}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Field: ${conflict.conflictField}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Local",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = conflict.localData.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Remote",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = conflict.remoteData.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Radio button option for resolution strategy.
 */
@Composable
private fun ResolutionStrategyOption(
    strategy: ConflictResolutionStrategy,
    label: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // Click handled by Row
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
