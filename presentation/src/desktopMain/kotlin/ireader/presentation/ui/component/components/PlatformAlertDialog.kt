package ireader.presentation.ui.component.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterialApi::class)
@Composable
actual fun PlatformAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier,
    dismissButton: @Composable() (() -> Unit)?,
    icon: @Composable() (() -> Unit)?,
    title: @Composable() () -> Unit,
    text: @Composable() (() -> Unit)?,
    shape: Shape,
    containerColor: Color,
    iconContentColor: Color,
    titleContentColor: Color,
    textContentColor: Color
) {

    AlertDialog(
        text = {
            Box(modifier = Modifier.width(350.dp).height(300.dp)) {
                if (text != null) {
                    text()
                }
            }

        },
        modifier = modifier,
        title = title,
        shape = shape,
        backgroundColor = containerColor,
        contentColor = textContentColor,
        buttons = {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                confirmButton()
                if (dismissButton != null) {
                    dismissButton()
                }
            }

        },
        onDismissRequest = onDismissRequest,
    )
}