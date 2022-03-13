package org.ireader.presentation.feature_library.presentation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.ireader.core.utils.Constants
import org.ireader.core_ui.theme.AppColors
import org.ireader.domain.view_models.library.LibraryEvents
import org.ireader.domain.view_models.library.LibraryViewModel
import org.ireader.presentation.presentation.ToolBar
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.TopAppBarSearch
import org.ireader.presentation.presentation.reusable_composable.TopAppBarTitle


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LibraryScreenTopBar(
    navController: NavController,
    viewModel: LibraryViewModel,
    coroutineScope: CoroutineScope,
    bottomSheetState: ModalBottomSheetState,
) {
    val state = viewModel.state
    val focusManager = LocalFocusManager.current

    ToolBar(
        title = {
            if (!state.inSearchMode) {
                TopAppBarTitle(title = "Library")
            } else {
                TopAppBarSearch(query = state.searchQuery,
                    onValueChange = {
                        viewModel.onEvent(LibraryEvents.UpdateSearchInput(it))
                        viewModel.onEvent(LibraryEvents.SearchBook(state.searchQuery))
                    },
                    onSearch = {
                        viewModel.onEvent(LibraryEvents.SearchBook(state.searchQuery))
                        focusManager.clearFocus()
                    },
                    isSearchModeEnable = state.searchQuery.isNotBlank())
            }
        },
        backgroundColor = AppColors.current.bars,
        contentColor = AppColors.current.onBars,
        elevation = Constants.DEFAULT_ELEVATION,
        actions = {
            if (state.inSearchMode) {
                AppIconButton(
                    imageVector = Icons.Default.Close,
                    title = "Close",
                    onClick = {
                        viewModel.onEvent(LibraryEvents.ToggleSearchMode(false))
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
                    viewModel.onEvent(LibraryEvents.ToggleSearchMode(true))
                },
            )


        },
        navigationIcon = if (state.inSearchMode) {
            {
                AppIconButton(imageVector = Icons.Default.ArrowBack,
                    title = "Toggle search mode off",
                    onClick = { viewModel.onEvent(LibraryEvents.ToggleSearchMode(false)) })

            }
        } else null

    )
}