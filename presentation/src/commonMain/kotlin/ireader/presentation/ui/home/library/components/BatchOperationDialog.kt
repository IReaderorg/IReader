package ireader.presentation.ui.home.library.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.DoneOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Enum representing available batch operations
 */
enum class BatchOperation {
    MARK_AS_READ,
    MARK_AS_UNREAD,
    DOWNLOAD,
    DOWNLOAD_UNREAD,
    DELETE,
    CHANGE_CATEGORY,
    ARCHIVE
}

/**
 * Main dialog for selecting batch operations
 */
@Composable
fun BatchOperationDialog(
    isVisible: Boolean,
    selectedCount: Int,
    onOperationSelected: (BatchOperation) -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = "Batch Operations ($selectedCount selected)")
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    BatchOperationItem(
                        icon = Icons.Outlined.Done,
                        title = "Mark as Read",
                        onClick = {
                            onOperationSelected(BatchOperation.MARK_AS_READ)
                            onDismiss()
                        }
                    )
                    
                    BatchOperationItem(
                        icon = Icons.Outlined.DoneOutline,
                        title = "Mark as Unread",
                        onClick = {
                            onOperationSelected(BatchOperation.MARK_AS_UNREAD)
                            onDismiss()
                        }
                    )
                    
                    BatchOperationItem(
                        icon = Icons.Outlined.Download,
                        title = "Download",
                        onClick = {
                            onOperationSelected(BatchOperation.DOWNLOAD)
                            onDismiss()
                        }
                    )
                    
                    BatchOperationItem(
                        icon = Icons.Outlined.DownloadForOffline,
                        title = "Download Unread",
                        onClick = {
                            onOperationSelected(BatchOperation.DOWNLOAD_UNREAD)
                            onDismiss()
                        }
                    )
                    
                    BatchOperationItem(
                        icon = Icons.Outlined.Delete,
                        title = "Delete",
                        onClick = {
                            onOperationSelected(BatchOperation.DELETE)
                            onDismiss()
                        }
                    )
                    
                    BatchOperationItem(
                        icon = Icons.Outlined.Label,
                        title = "Change Category",
                        onClick = {
                            onOperationSelected(BatchOperation.CHANGE_CATEGORY)
                            onDismiss()
                        }
                    )
                    
                    BatchOperationItem(
                        icon = Icons.Outlined.Archive,
                        title = "Archive",
                        onClick = {
                            onOperationSelected(BatchOperation.ARCHIVE)
                            onDismiss()
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Individual batch operation item
 */
@Composable
private fun BatchOperationItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
//            Text(
//                text = title,
//                style = MaterialTheme.typography.bodyLarge,
//                modifier = Modifier.weight(1f)
//            )
        }
    }
}

/**
 * Dialog showing progress of batch operations
 */
@Composable
fun BatchOperationProgressDialog(
    isVisible: Boolean,
    message: String,
    onDismiss: () -> Unit = {}
) {
    if (isVisible) {
        Dialog(onDismissRequest = { /* Prevent dismissal during operation */ }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Dialog showing result of batch operations
 */
@Composable
fun BatchOperationResultDialog(
    isVisible: Boolean,
    title: String,
    message: String,
    showUndo: Boolean = false,
    onUndo: () -> Unit = {},
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = title)
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            },
            dismissButton = if (showUndo) {
                {
                    TextButton(onClick = {
                        onUndo()
                        onDismiss()
                    }) {
                        Text("UNDO")
                    }
                }
            } else null
        )
    }
}
