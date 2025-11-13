package ireader.presentation.ui.component.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import ireader.presentation.ui.core.theme.Shapes

/**
 * Consolidated dialog components to reduce duplication across the codebase
 */

/**
 * Standard confirmation dialog with title, message, and confirm/cancel buttons
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
) {
    IAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        icon = icon,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        modifier = modifier
    )
}

/**
 * Information dialog with only a dismiss button
 */
@Composable
fun InfoDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    dismissText: String = "OK",
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
) {
    IAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        icon = icon,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        modifier = modifier
    )
}

/**
 * Error dialog with error styling
 */
@Composable
fun ErrorDialog(
    title: String = "Error",
    message: String,
    onDismiss: () -> Unit,
    dismissText: String = "OK",
    modifier: Modifier = Modifier,
) {
    IAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = MaterialTheme.colorScheme.error) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        modifier = modifier
    )
}

/**
 * Loading dialog that cannot be dismissed
 */
@Composable
fun LoadingDialog(
    message: String = "Loading...",
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = { /* Cannot dismiss */ },
        title = { Text(message) },
        text = {
            LinearProgressIndicator(modifier = Modifier)
        },
        confirmButton = { },
        modifier = modifier
    )
}

/**
 * Custom dialog with flexible content
 */
@Composable
fun CustomDialog(
    onDismissRequest: () -> Unit,
    title: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    shape: Shape = Shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surface,
) {
    IAlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = content,
        icon = icon,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        modifier = modifier,
        shape = shape,
        containerColor = containerColor
    )
}
