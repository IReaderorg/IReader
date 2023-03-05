package ireader.presentation.ui.component.reusable_composable

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import ireader.presentation.ui.component.components.IAlertDialog

class WarningAlertData {
    val enable = mutableStateOf(false)
    val title: MutableState<String?> = mutableStateOf(null)
    val text: MutableState<String?> = mutableStateOf(null)
    val onConfirm: MutableState<(() -> Unit)?> = mutableStateOf(null)
    val onDismiss: MutableState<(() -> Unit)?> = mutableStateOf(null)
    val confirmText: MutableState<String> = mutableStateOf("Confirm")
    val dismissText: MutableState<String> = mutableStateOf("Cancel")
}

@Composable
fun WarningAlert(
    data: WarningAlertData,
) {
    if (data.enable.value) {
        IAlertDialog(
            title = if (data.title.value != null) {
                {
                    MidSizeTextComposable(text = data.title.value!!)
                }
            } else null,
            text = if (data.text.value != null) {
                {
                    MidSizeTextComposable(text = data.text.value!!)
                }
            } else null,
            onDismissRequest = {
                data.onDismiss.value?.invoke()
            },
            confirmButton = {
                TextButton(onClick = { data.onConfirm.value?.invoke() }) {
                    Text(text = data.confirmText.value)
                }
            },
            dismissButton = {
                TextButton(onClick = { data.onDismiss.value?.invoke() }) {
                    Text(text = data.dismissText.value)
                }
            },
        )
    }
}
