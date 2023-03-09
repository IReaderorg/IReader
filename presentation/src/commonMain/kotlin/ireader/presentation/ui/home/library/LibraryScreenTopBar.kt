package ireader.presentation.ui.home.library

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.AppTextField
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.core.ui.DEFAULT_ELEVATION
import ireader.presentation.ui.home.library.viewmodel.LibraryState
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreenTopBar(
        state: LibraryState,
        onSearch: (() -> Unit)? = null,
        refreshUpdate: () -> Unit,
        onClickSelectAll: () -> Unit,
        onClickInvertSelection: () -> Unit,
        onClearSelection: () -> Unit,
        scrollBehavior: TopAppBarScrollBehavior? = null,
        showModalSheet:() -> Unit,
        hideModalSheet:() -> Unit,
        isModalVisible:Boolean
) {
    when {
        state.selectionMode -> {
            EditModeTopAppBar(
                selectionSize = state.selectedBooks.size,
                onClickCancelSelection = onClearSelection,
                onClickSelectAll = onClickSelectAll,
                onClickInvertSelection = onClickInvertSelection,
                scrollBehavior = scrollBehavior
            )
        }
        else -> {
            RegularTopBar(
                state,
                refreshUpdate = refreshUpdate,
                onSearch = {
                    onSearch?.invoke()
                },
                scrollBehavior = scrollBehavior,
                    hideModalSheet = hideModalSheet,
                    isModalVisible = isModalVisible,
                    showModalSheet = showModalSheet
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
private fun RegularTopBar(
        vm: LibraryState,
        onSearch: () -> Unit,
        refreshUpdate: () -> Unit,
        scrollBehavior: TopAppBarScrollBehavior? = null,
        showModalSheet:() -> Unit,
        hideModalSheet:() -> Unit,
        isModalVisible:Boolean
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Toolbar(
        title = {
            if (!vm.inSearchMode) {
                BigSizeTextComposable(text = localize(MR.strings.library))
            } else {
                AppTextField(
                    query = vm.searchQuery ?: "",
                    onValueChange = { query ->
                        vm.searchQuery = query
                        onSearch()
                    },
                    onConfirm = {
                        onSearch()
                        vm.inSearchMode = false
                        vm.searchQuery = null
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
                    contentDescription = localize(MR.strings.close),
                    onClick = {
                        vm.inSearchMode = false
                        vm.searchQuery = null
                        onSearch()
                    },
                )
            } else {
                AppIconButton(
                    imageVector = Icons.Default.Sort,
                    contentDescription = localize(MR.strings.filter),
                    onClick = {
                        scope.launch {
                            if (isModalVisible) {
                                hideModalSheet()
                            } else {
                                showModalSheet()
                            }
                        }
                    },
                )
                AppIconButton(
                    imageVector = Icons.Default.Search,
                    contentDescription = localize(MR.strings.search),
                    onClick = {
                        vm.inSearchMode = true
                    },
                )
                AppIconButton(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = localize(MR.strings.refresh),
                    onClick = {
                        refreshUpdate()
                    },
                )
            }
        },
        navigationIcon = {
            if (vm.inSearchMode) {
                TopAppBarBackButton {
                    vm.inSearchMode = false
                    vm.searchQuery = null
                }
            } else null
        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditModeTopAppBar(
    selectionSize: Int,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Toolbar(
        title = { BigSizeTextComposable(text = "$selectionSize") },
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
        },
        scrollBehavior = scrollBehavior
    )
}
