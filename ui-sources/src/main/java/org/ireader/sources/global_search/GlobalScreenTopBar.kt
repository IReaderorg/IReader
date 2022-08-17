package org.ireader.sources.global_search

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.AppTextField
import org.ireader.sources.global_search.viewmodel.GlobalSearchState
import org.ireader.ui_sources.R

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun GlobalScreenTopBar(
    onSearch: (String) -> Unit,
    onPop: () -> Unit,
    state: GlobalSearchState,
    scrollBehavior: TopAppBarScrollBehavior?
) {

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Toolbar(
        scrollBehavior = scrollBehavior,
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
                contentDescription = stringResource(R.string.search),
                onClick = {
                    state.searchMode = true
                },
            )
        },
        navigationIcon = {
            AppIconButton(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.toggle_search_mode_off),
                onClick = {
                    onPop()
                }
            )
        }

    )
}
