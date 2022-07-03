package org.ireader.core_ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun WarningAlert(
    show:Boolean,
    text:String,
    title:String,
    onConfirm:(() -> Unit)? = null,
    onDismiss:(() -> Unit)? = null,
) {
    AnimatedVisibility(visible = show) {
        AlertDialog(
            modifier = Modifier.heightIn(max = 350.dp, min = 200.dp),
            onDismissRequest = {
                if (onDismiss != null) {
                    onDismiss()
                }
            },
            title = { Text(title) },
            text = {
                Text(text = text)
            },
            confirmButton = {
                TextButton(onClick = {
                    if (onConfirm != null) {
                        onConfirm()
                    }
                }) {
                    Text(text = stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                        if (onDismiss != null) {
                            onDismiss()
                        }
                }) {
                    Text(text = stringResource(R.string.dismiss))
                }
            }
        )
    }

}