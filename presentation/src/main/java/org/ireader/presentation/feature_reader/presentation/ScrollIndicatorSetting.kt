package org.ireader.presentation.feature_reader.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import org.ireader.domain.view_models.reader.ReaderScreenViewModel
import org.ireader.presentation.presentation.reusable_composable.AppTextField
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.utils.scroll.rememberCarouselScrollState
import org.ireader.presentation.utils.scroll.verticalScroll

@Composable
fun ScrollIndicatorSetting(
    enable: Boolean = false, vm: ReaderScreenViewModel,
    onDismiss: () -> Unit = {
        vm.scrollIndicatorDialogShown = false
        vm.scrollIndicatorPadding = vm.readScrollIndicatorPadding()
        vm.scrollIndicatorWith = vm.readScrollIndicatorWidth()
        vm.readBackgroundColor()
        vm.readTextColor()
    },
) {
    val (pValue, setPaddingValue) = remember { mutableStateOf<String>("") }
    val (wValue, setWidthValue) = remember { mutableStateOf<String>("") }
    val (bgValue, setBGValue) = remember { mutableStateOf<String>("") }
    val (txtValue, setTxtValue) = remember { mutableStateOf<String>("") }
    val (autoScrollText, setAutoScrollValue) = remember { mutableStateOf<String>("") }
    val (autoScrollOffsetText, setAutoScrolOffsetlValue) = remember { mutableStateOf<String>("") }
    val focusManager = LocalFocusManager.current

    if (enable) {
        AlertDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = {
                onDismiss()
            },
            title = null,
            text = {
                Column(modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .verticalScroll(
                        rememberCarouselScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    MidSizeTextComposable(text = "Advance Setting")
                    Spacer(modifier = Modifier.height(32.dp))
                    AppTextField(
                        query = pValue,
                        onValueChange = {
                            setPaddingValue(it)
                            try {
                                vm.scrollIndicatorPadding = it.toInt()
                            } catch (e: Exception) {
                            }

                        },
                        onConfirm = {
                            focusManager.clearFocus()
                        },
                        hint = "Scroll Indicator Padding Value",
                        mode = 2,
                        keyboardAction = KeyboardOptions(imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppTextField(
                        query = wValue,
                        onValueChange = {
                            setWidthValue(it)
                            try {
                                vm.scrollIndicatorWith = it.toInt()
                            } catch (e: Exception) {
                            }

                        },
                        onConfirm = {
                            focusManager.clearFocus()
                        },
                        hint = "Scroll Indicator  Width Value",
                        mode = 2,
                        keyboardAction = KeyboardOptions(imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppTextField(
                        query = bgValue,
                        onValueChange = {
                            setBGValue(it)
                            try {
                                vm.backgroundColor = Color(it.toColorInt())
                            } catch (e: Exception) {
                                vm.readBackgroundColor()
                            }

                        },
                        onConfirm = {
                            focusManager.clearFocus()
                        },
                        hint = "Background Color",
                        mode = 2,
                        keyboardAction = KeyboardOptions(imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Text)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppTextField(
                        query = txtValue,
                        onValueChange = {
                            setTxtValue(it)
                            try {
                                vm.textColor = Color(it.toColorInt())
                            } catch (e: Exception) {
                                vm.readTextColor()
                            }
                        },
                        onConfirm = {
                            focusManager.clearFocus()
                        },
                        hint = "TextColor Value",
                        mode = 2,
                        keyboardAction = KeyboardOptions(imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Text)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppTextField(
                        query = autoScrollText,
                        onValueChange = {
                            setAutoScrollValue(it)
                        },
                        onConfirm = {
                            focusManager.clearFocus()
                        },
                        hint = "Auto Scroll Interval (Milli Seconds)",
                        mode = 2,
                        keyboardAction = KeyboardOptions(imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Text)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppTextField(
                        query = autoScrollOffsetText,
                        onValueChange = {
                            setAutoScrolOffsetlValue(it)
                        },
                        onConfirm = {
                            focusManager.clearFocus()
                        },
                        hint = "Auto Scroll Offset",
                        mode = 2,
                        keyboardAction = KeyboardOptions(imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Text)
                    )
                }

            },
            contentColor = MaterialTheme.colors.onBackground,
            backgroundColor = MaterialTheme.colors.background,
            buttons = {
                Row(horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = {
                        vm.scrollIndicatorDialogShown = false
                        onDismiss()
                    },
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = MaterialTheme.colors.background,
                            contentColor = MaterialTheme.colors.onBackground
                        )) {

                        MidSizeTextComposable(text = "DISMISS")
                    }
                    Button(onClick = {
                        vm.scrollIndicatorDialogShown = false
                        try {

                            if (pValue.isNotBlank()) {
                                vm.saveScrollIndicatorPadding(pValue.toInt())
                            }
                        } catch (e: Exception) {
                        }

                        try {
                            if (wValue.isNotBlank()) {
                                vm.saveScrollIndicatorWidth(wValue.toInt())
                            }
                        } catch (e: Exception) {
                        }


                        try {
                            if (bgValue.isNotBlank()) {
                                vm.setReaderBackgroundColor(vm.backgroundColor)
                            }
                        } catch (e: Exception) {
                        }

                        try {
                            if (txtValue.isNotBlank()) {
                                vm.setReaderTextColor(vm.textColor)
                            }
                        } catch (e: Exception) {
                        }

                        try {
                            if (autoScrollText.isNotBlank()) {
                                vm.setAutoScrollIntervalReader(autoScrollText.toLong())
                            }
                        } catch (e: Exception) {
                        }
                        try {
                            if (autoScrollOffsetText.isNotBlank()) {
                                vm.setAutoScrollOffsetReader(autoScrollOffsetText.toInt())
                            }
                        } catch (e: Exception) {
                        }


                    },
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = MaterialTheme.colors.background
                        )) {

                        MidSizeTextComposable(text = "APPLY")
                    }
                }

            },
        )
    }
}


