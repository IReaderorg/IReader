package ireader.presentation.ui.component.components.component

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape



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
){
    androidx.compose.material.AlertDialog(
        buttons = {
            confirmButton()
            if (dismissButton != null) {
                dismissButton()
            }
        },
        onDismissRequest = onDismissRequest
    )
}