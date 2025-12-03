package ireader.presentation.ui.component.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import ireader.presentation.ui.core.theme.Shapes

@Composable
fun IAlertDialog(
    onDismissRequest: () -> Unit = {},
    confirmButton: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    dismissButton: @Composable() (() -> Unit)? = null,
    icon: @Composable() (() -> Unit)? = null,
    title: @Composable() (() -> Unit)? = {},
    text: @Composable() (() -> Unit)? = {},
    shape: Shape = Shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    iconContentColor: Color = contentColorFor(MaterialTheme.colorScheme.surface),
    titleContentColor: Color = contentColorFor(MaterialTheme.colorScheme.surface),
    textContentColor: Color = contentColorFor(MaterialTheme.colorScheme.surface),
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        shape = shape,
        containerColor = containerColor,
        iconContentColor = iconContentColor,

    )
}
