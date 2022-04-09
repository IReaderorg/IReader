package org.ireader.presentation.feature_library.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.ireader.core.utils.Constants
import org.ireader.core_ui.theme.AppColors
import org.ireader.presentation.feature_library.presentation.viewmodel.LibraryEvents
import org.ireader.presentation.feature_library.presentation.viewmodel.LibraryViewModel
import org.ireader.presentation.presentation.Toolbar
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.AppTextField
import org.ireader.presentation.presentation.reusable_composable.BigSizeTextComposable


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LibraryScreenTopBar(
    navController: NavController,
    state: LibraryViewModel,
    coroutineScope: CoroutineScope,
    bottomSheetState: ModalBottomSheetState,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        when {
            state.hasSelection -> {
                EditModeTopAppBar(
                    selectionSize = state.selection.size,
                    onClickCancelSelection = { state.selection.clear() },
                    onClickSelectAll = {
                        state.selection.clear()
                        state.selection.addAll(state.books.map { it.id })
                        state.selection.distinct()
                    },
                    onClickInvertSelection = {
                        val ids: List<Long> =
                            state.books.map { it.id }
                                .filterNot { it in state.selection }.distinct()
                        state.selection.clear()
                        state.selection.addAll(ids)
                    }
                )
            }
            else -> {
                RegularTopBar(
                    state, bottomSheetState
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun RegularTopBar(
    vm: LibraryViewModel,
    bottomSheetState: ModalBottomSheetState,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    Toolbar(
        title = {
            if (!vm.inSearchMode) {
                BigSizeTextComposable(text = "Library")
            } else {
                AppTextField(
                    query = vm.searchQuery,
                    onValueChange = {
                        vm.onEvent(LibraryEvents.UpdateSearchInput(it))
                        vm.onEvent(LibraryEvents.SearchBook(vm.searchQuery))
                    },
                    onConfirm = {
                        vm.onEvent(LibraryEvents.SearchBook(vm.searchQuery))
                        focusManager.clearFocus()
                    },
                )
            }
        },
        backgroundColor = AppColors.current.bars,
        contentColor = AppColors.current.onBars,
        elevation = Constants.DEFAULT_ELEVATION,
        actions = {
            if (vm.inSearchMode) {
                AppIconButton(
                    imageVector = Icons.Default.Close,
                    title = "Close",
                    onClick = {
                        vm.onEvent(LibraryEvents.ToggleSearchMode(false))
                    },
                )
            } else {
                AppIconButton(
                    imageVector = Icons.Default.Sort,
                    title = "Filter",
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
                    title = "Search",
                    onClick = {
                        vm.onEvent(LibraryEvents.ToggleSearchMode(true))
                    },
                )
                AppIconButton(
                    imageVector = Icons.Default.Refresh,
                    title = "Refresh",
                    onClick = {
                        vm.refreshUpdate(context = context)
                    },
                )
            }

        },
        navigationIcon = if (vm.inSearchMode) {
            {
                AppIconButton(imageVector = Icons.Default.ArrowBack,
                    title = "Toggle search mode off",
                    onClick = { vm.onEvent(LibraryEvents.ToggleSearchMode(false)) })

            }
        } else null

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
        title = { BigSizeTextComposable(text = "$selectionSize") },
        navigationIcon = {
            IconButton(onClick = onClickCancelSelection) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        },
        elevation = Constants.DEFAULT_ELEVATION,
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