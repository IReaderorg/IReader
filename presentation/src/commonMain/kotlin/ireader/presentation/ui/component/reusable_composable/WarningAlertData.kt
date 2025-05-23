package ireader.presentation.ui.component.reusable_composable

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.i18n.UiText
import ireader.i18n.asString
import ireader.i18n.localize
import ireader.presentation.ui.component.components.IAlertDialog
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

class WarningAlertData {
    var enable by mutableStateOf(false)
    var title: MutableState<String?> = mutableStateOf(null)
    var text: MutableState<String?> = mutableStateOf(null)
    var onConfirm: MutableState<(() -> Unit)?> = mutableStateOf(null)
    var onDismiss: MutableState<(() -> Unit)?> = mutableStateOf(null)
    val confirmText: MutableState<String> = mutableStateOf("Confirm")
    val dismissText: MutableState<String> = mutableStateOf("Cancel")

    fun copy(
        text: String? = this.text.value,
        enable: Boolean, title: String? = this.title.value,
        onConfirm: (() -> Unit)? = this.onConfirm.value,
        onDismiss: (() -> Unit)? = this.onDismiss.value,
        confirmText: String = this.confirmText.value,
        dismissText: String = this.dismissText.value,
    ): WarningAlertData {
        val copy = WarningAlertData()
        copy.enable = enable
        copy.title.value = title
        copy.text.value = text
        copy.onConfirm.value = onConfirm
        copy.onDismiss.value = onDismiss
        copy.confirmText.value = confirmText
        copy.dismissText.value = dismissText
        return copy
    }


}

@Composable
fun WarningAlert(
    data: WarningAlertData,
) {
    if (data.enable) {
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
