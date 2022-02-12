package org.ireader.presentation.feature_library.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.ireader.core.utils.Constants
import org.ireader.core_ui.theme.AppColors
import org.ireader.domain.view_models.library.LibraryEvents
import org.ireader.domain.view_models.library.LibraryViewModel
import org.ireader.presentation.presentation.reusable_composable.TopAppBarActionButton
import org.ireader.presentation.presentation.reusable_composable.TopAppBarSearch
import org.ireader.presentation.presentation.reusable_composable.TopAppBarTitle

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LibraryScreenTopBar(
    navController: NavController,
    viewModel: LibraryViewModel,
    coroutineScope: CoroutineScope,
    bottomSheetState: BottomSheetScaffoldState,
) {
    val state = viewModel.state
    val focusManager = LocalFocusManager.current
    TopAppBar(
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
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = AppColors.current.bars,
        contentColor = AppColors.current.onBars,
        elevation = Constants.DEFAULT_ELEVATION,
        actions = {
            if (state.inSearchMode) {
                TopAppBarActionButton(
                    imageVector = Icons.Default.Close,
                    title = "Close",
                    onClick = {
                        viewModel.onEvent(LibraryEvents.ToggleSearchMode(false))
                    },
                )
            }
            TopAppBarActionButton(
                imageVector = Icons.Default.Sort,
                title = "Filter",
                onClick = {
                    coroutineScope.launch {
                        if (bottomSheetState.bottomSheetState.isExpanded) {
                            bottomSheetState.bottomSheetState.collapse()
                        } else {
                            bottomSheetState.bottomSheetState.expand()
                        }
                    }
                },
            )
            TopAppBarActionButton(
                imageVector = Icons.Default.Search,
                title = "Search",
                onClick = {
                    viewModel.onEvent(LibraryEvents.ToggleSearchMode(true))
                },
            )


        },
        navigationIcon = if (state.inSearchMode) {
            {
                TopAppBarActionButton(imageVector = Icons.Default.ArrowBack,
                    title = "Toggle search mode off",
                    onClick = { viewModel.onEvent(LibraryEvents.ToggleSearchMode(false)) })

            }
        } else null

    )
}