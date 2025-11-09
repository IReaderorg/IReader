package ireader.presentation.ui.home.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

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
