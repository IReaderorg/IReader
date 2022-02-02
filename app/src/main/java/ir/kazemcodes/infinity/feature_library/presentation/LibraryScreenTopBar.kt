package ir.kazemcodes.infinity.feature_library.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarActionButton
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarBackButton
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarSearch
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarTitle
import ir.kazemcodes.infinity.core.presentation.theme.Colour.topBarColor
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.feature_library.presentation.components.LibraryEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LibraryScreenTopBar(
    navController: NavController,
    viewModel: LibraryViewModel,
    coroutineScope: CoroutineScope,
    modalBottomSheetState: ModalBottomSheetState
) {
    val state = viewModel.state.value
    val focusManager = LocalFocusManager.current
    TopAppBar(
        title = {
            if (!state.inSearchMode) {
                TopAppBarTitle(title = "Library")
            } else {
                TopAppBarSearch(query = state.searchQuery,
                    onValueChange = {
                        viewModel.onEvent(LibraryEvents.UpdateSearchInput(it))
                    },
                    onSearch = {
                        viewModel.searchBook(state.searchQuery)
                        focusManager.clearFocus()
                    },
                    isSearchModeEnable = state.searchQuery.isNotBlank())
            }
        },
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.topBarColor,
        contentColor = MaterialTheme.colors.onBackground,
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
                        if (!modalBottomSheetState.isVisible) {
                          modalBottomSheetState.show()
                            //bottomSheetScaffoldState.bottomSheetState.expand()
                        } else {
                            modalBottomSheetState.hide()
                            //viewModel.bottomSheetState.value = BottomSheetValue.Collapsed
                            //bottomSheetScaffoldState.bottomSheetState.collapse()
                        }
                    }
                },
            )
            TopAppBarActionButton(
                imageVector = Icons.Default.Search,
                title = "Search",
                onClick = {
                    viewModel.onEvent(LibraryEvents.ToggleSearchMode())

                },
            )


        },
        navigationIcon = if (state.inSearchMode) {
            {
                TopAppBarBackButton(navController = navController,
                    onClick = {
                        viewModel.onEvent(LibraryEvents.ToggleSearchMode(false))
                    })
            }
        } else null

    )
}