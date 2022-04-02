package org.ireader.presentation.feature_library.presentation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
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
import org.ireader.presentation.presentation.reusable_composable.TopAppBarTitle


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LibraryScreenTopBar(
    navController: NavController,
    vm: LibraryViewModel,
    coroutineScope: CoroutineScope,
    bottomSheetState: ModalBottomSheetState,
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Toolbar(
        title = {
            if (!vm.inSearchMode) {
                TopAppBarTitle(title = "Library")
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
            }

            AppIconButton(
                imageVector = Icons.Default.Sort,
                title = "Filter",
                onClick = {
                    coroutineScope.launch {
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