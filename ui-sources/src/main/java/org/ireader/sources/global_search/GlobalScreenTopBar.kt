package org.ireader.sources.global_search

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.AppTextField
import org.ireader.sources.global_search.viewmodel.GlobalSearchState

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun GlobalScreenTopBar(
    onSearch: (String) -> Unit,
    onPop: () -> Unit,
    state: GlobalSearchState,
) {

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Toolbar(
        title = {
            AppTextField(
                query = state.query,
                onValueChange = {
                    state.query = it
                },
                onConfirm = {
                    onSearch(state.query)
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
            )
        },
        actions = {
            AppIconButton(
                imageVector = Icons.Default.Search,
                title = "Search",
                onClick = {
                    state.searchMode = true
                },
            )
        },
        navigationIcon = {
            AppIconButton(
                imageVector = Icons.Default.ArrowBack,
                title = "Toggle search mode off",
                onClick = {
                    onPop()
                }
            )
        }

    )
}
