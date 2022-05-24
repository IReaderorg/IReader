package org.ireader.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.ireader.app.viewmodel.LibraryState
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.AppTextField
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.core_ui.ui.DEFAULT_ELEVATION
import org.ireader.ui_library.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LibraryScreenTopBar(
    state: LibraryState,
    bottomSheetState: ModalBottomSheetState,
    onSearch: () -> Unit,
    refreshUpdate: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    onClearSelection:() -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        when {
            state.hasSelection -> {
                EditModeTopAppBar(
                    selectionSize = state.selection.size,
                    onClickCancelSelection = onClearSelection,
                    onClickSelectAll = onClickSelectAll,
                    onClickInvertSelection = onClickInvertSelection
                )
            }
            else -> {
                RegularTopBar(
                    state,
                    bottomSheetState,
                    refreshUpdate = refreshUpdate,
                    onSearch = onSearch
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun RegularTopBar(
    vm: LibraryState,
    bottomSheetState: ModalBottomSheetState,
    onSearch: () -> Unit,
    refreshUpdate: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Toolbar(
        title = {
            if (!vm.inSearchMode) {
                BigSizeTextComposable(text = stringResource(R.string.library))
            } else {
                AppTextField(
                    query = vm.searchQuery?:"",
                    onValueChange = { query ->
                        vm.searchQuery = query
                        onSearch()
                    },
                    onConfirm = {
                        onSearch()
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                )
            }
        },
        actions = {
            if (vm.inSearchMode) {
                AppIconButton(
                    imageVector = Icons.Default.Close,
                   contentDescription = stringResource( R.string.close),
                    onClick = {
                        vm.inSearchMode = false
                        vm.searchQuery = ""
                        onSearch()
                    },
                )
            } else {
                AppIconButton(
                    imageVector = Icons.Default.Sort,
                    contentDescription = stringResource( R.string.filter),
                    onClick = {
                        scope.launch {
                            if (bottomSheetState.isVisible) {
                                bottomSheetState.hide()
                            } else {
                                bottomSheetState.show()
                            }
                        }
                    },
                )
                AppIconButton(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource( R.string.search),
                    onClick = {
                        vm.inSearchMode = true
                    },
                )
                AppIconButton(
                    imageVector = Icons.Default.Refresh,
                   contentDescription = stringResource( R.string.refresh),
                    onClick = {
                        refreshUpdate()
                    },
                )
            }
        },
        navigationIcon = {
            if (vm.inSearchMode) {

                    AppIconButton(
                        imageVector = Icons.Default.ArrowBack,
                       contentDescription = stringResource( R.string.toggle_search_mode_off),
                        onClick = {
                            vm.inSearchMode = false
                        }
                    )
            } else null
        }

    )
}

@Composable
private fun EditModeTopAppBar(
    selectionSize: Int,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
) {
    Toolbar(
        title = { BigSizeTextComposable(text ="$selectionSize") },
        navigationIcon = {
            IconButton(onClick = onClickCancelSelection) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        },
        elevation = DEFAULT_ELEVATION,
        actions = {
            IconButton(onClick = onClickSelectAll) {
                Icon(Icons.Default.SelectAll, contentDescription = null)
            }
            IconButton(onClick = onClickInvertSelection) {
                Icon(Icons.Default.FlipToBack, contentDescription = null)
            }
        }
    )
}
