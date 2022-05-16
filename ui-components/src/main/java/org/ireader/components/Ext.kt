package org.ireader.components

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.SoftwareKeyboardController

@OptIn(ExperimentalComposeUiApi::class)
fun hideKeyboard(softwareKeyboardController: SoftwareKeyboardController?, focusManager: FocusManager) {
    softwareKeyboardController?.hide()
    focusManager.clearFocus()
}