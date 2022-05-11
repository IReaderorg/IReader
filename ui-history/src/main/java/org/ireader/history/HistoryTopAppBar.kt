package org.ireader.history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import org.ireader.common_resources.UiText
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.AppTextField
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.history.viewmodel.HistoryState
import org.ireader.ui_history.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HistoryTopAppBar(
    vm: HistoryState,
    onDeleteAll: () -> Unit,
    getHistories: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Toolbar(
        title = {
            if (!vm.searchMode) {
                BigSizeTextComposable(text = UiText.StringResource(R.string.history_screen_label))
            } else {
                AppTextField(
                    query = vm.searchQuery,
                    onValueChange = {
                        vm.searchQuery = it
                        getHistories()
                    },
                    onConfirm = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                )
            }
        },
        actions = {
            if (vm.searchMode) {
                AppIconButton(
                    imageVector = Icons.Default.Close,
                    text = UiText.StringResource(R.string.close),
                    onClick = {
                        vm.searchMode = false
                        vm.searchQuery = ""
                        getHistories()
                        keyboardController?.hide()
                    },
                )
            }
            AppIconButton(
                imageVector = Icons.Default.Search,
                text = UiText.StringResource(R.string.search),
                onClick = {
                    vm.searchMode = true
                },
            )
            AppIconButton(
                imageVector = Icons.Default.Delete,
                text = UiText.StringResource(R.string.delete_all_histories),
                onClick = {
                    onDeleteAll()
                },
            )
        },
        navigationIcon = if (vm.searchMode) {
            {
                AppIconButton(
                    imageVector = Icons.Default.ArrowBack,
                    text = UiText.StringResource(R.string.toggle_search_mode_off),
                    onClick = {
                        vm.searchMode = false
                        vm.searchQuery = ""
                        keyboardController?.hide()
                    }
                )
            }
        } else null

    )
}
