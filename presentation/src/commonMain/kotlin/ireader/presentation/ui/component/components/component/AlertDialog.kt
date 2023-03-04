package ireader.presentation.ui.component.components.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import ireader.presentation.ui.core.theme.Shapes


@Composable
expect fun PlatformAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier,
    dismissButton: @Composable() (() -> Unit)?,
    icon: @Composable() (() -> Unit)?,
    title: @Composable() (() -> Unit),
    text: @Composable() (() -> Unit)?,
    shape: Shape,
    containerColor: Color,
    iconContentColor: Color,
    titleContentColor: Color,
    textContentColor: Color,
)

@Composable
fun IAlertDialog(
    onDismissRequest: () -> Unit = {},
    confirmButton: @Composable () -> Unit= {},
    modifier: Modifier = Modifier,
    dismissButton: @Composable() (() -> Unit)? = null,
    icon: @Composable() (() -> Unit)? = null,
    title: @Composable() (() -> Unit)? = {},
    text: @Composable() (() -> Unit)? = {},
    shape: Shape = Shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    iconContentColor: Color =contentColorFor(MaterialTheme.colorScheme.surface),
    titleContentColor: Color =contentColorFor(MaterialTheme.colorScheme.surface),
    textContentColor: Color = contentColorFor(MaterialTheme.colorScheme.surface),
) = PlatformAlertDialog(
    onDismissRequest = {},
    confirmButton = {},
    modifier = Modifier,
    dismissButton = null,
    icon = null,
    title = {},
    text = null,
    shape = Shapes.medium,
    containerColor = MaterialTheme.colorScheme.surface,
    iconContentColor = contentColorFor(MaterialTheme.colorScheme.surface),
    titleContentColor = contentColorFor(MaterialTheme.colorScheme.surface),
    textContentColor = contentColorFor(MaterialTheme.colorScheme.surface),
)