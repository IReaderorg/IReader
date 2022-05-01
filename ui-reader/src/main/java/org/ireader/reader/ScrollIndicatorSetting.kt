package org.ireader.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.ireader.components.reusable_composable.AppTextField
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.reader.viewmodel.ReaderScreenPreferencesState

@Composable
fun ScrollIndicatorSetting(
    enable: Boolean = false,
    vm: ReaderScreenPreferencesState,
    onDismiss: () -> Unit,
    onBackgroundColorValueChange:(String) -> Unit,
    onTextColorValueChange:(String) -> Unit,
    onBackgroundColorAndTextColorApply:(bgColor:String,txtColor:String) -> Unit
) {

    val (bgValue, setBGValue) = remember { mutableStateOf<String>("") }
    val (txtValue, setTxtValue) = remember { mutableStateOf<String>("") }
    val focusManager = LocalFocusManager.current

    if (enable) {
        AlertDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = {
                onDismiss()
            },
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    MidSizeTextComposable(text = "Advance Setting")
                    Spacer(modifier = Modifier.height(32.dp))
                    AppTextField(
                        query = bgValue,
                        onValueChange = {
                            setBGValue(it)
                            onBackgroundColorValueChange(it)
                        },
                        onConfirm = {
                            focusManager.clearFocus()
                        },
                        hint = "Background Color",
                        mode = 2,
                        keyboardAction = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Text
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppTextField(
                        query = txtValue,
                        onValueChange = {
                            setTxtValue(it)
                            onTextColorValueChange(it)
                        },
                        onConfirm = {
                            focusManager.clearFocus()
                        },
                        hint = "TextColor Value",
                        mode = 2,
                        keyboardAction = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Text
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            },
            contentColor = MaterialTheme.colors.onBackground,
            backgroundColor = MaterialTheme.colors.background,
            buttons = {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            vm.scrollIndicatorDialogShown = false
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = MaterialTheme.colors.background,
                            contentColor = MaterialTheme.colors.onBackground
                        )
                    ) {

                        MidSizeTextComposable(text = "DISMISS")
                    }
                    Button(
                        onClick = {
                            vm.scrollIndicatorDialogShown = false
                            onBackgroundColorAndTextColorApply(bgValue,txtValue)
                        },
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = MaterialTheme.colors.background
                        )
                    ) {

                        MidSizeTextComposable(text = "APPLY")
                    }
                }
            },
        )
    }
}
