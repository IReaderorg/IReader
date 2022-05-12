package org.ireader.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.ireader.common_resources.UiText
import org.ireader.components.reusable_composable.AppTextField
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.reader.viewmodel.ReaderScreenPreferencesState
import org.ireader.ui_reader.R

@Composable
fun ScrollIndicatorSetting(
    enable: Boolean = false,
    vm: ReaderScreenPreferencesState,
    onDismiss: () -> Unit,
    onBackgroundColorValueChange: (String) -> Unit,
    onTextColorValueChange: (String) -> Unit,
    onBackgroundColorAndTextColorApply: (bgColor: String, txtColor: String) -> Unit
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
                    MidSizeTextComposable(text = UiText.StringResource(R.string.advance_setting))
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
                        hint = UiText.StringResource(R.string.background_color),
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
                        hint = UiText.StringResource(R.string.text_color),
                        mode = 2,
                        keyboardAction = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Text
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            },
            textContentColor = MaterialTheme.colorScheme.onBackground,
            containerColor = MaterialTheme.colorScheme.background,
            confirmButton = {

                Button(
                    onClick = {
                        vm.scrollIndicatorDialogShown = false
                        onBackgroundColorAndTextColorApply(bgValue, txtValue)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {

                    MidSizeTextComposable(text = UiText.StringResource(R.string.apply))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        vm.scrollIndicatorDialogShown = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {

                    MidSizeTextComposable(text = UiText.StringResource(R.string.dismiss))
                }
            }
        )
    }
}
