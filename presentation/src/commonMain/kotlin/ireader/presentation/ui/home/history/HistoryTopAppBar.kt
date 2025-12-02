package ireader.presentation.ui.home.history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.AppTextField
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.home.history.viewmodel.HistoryViewModel

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryTopAppBar(
    modifier: Modifier = Modifier,
    vm: HistoryViewModel,
    onDeleteAll: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    // Collect state reactively
    val state by vm.state.collectAsState()
    val searchMode = state.isSearchMode
    val searchQuery = state.searchQuery
    
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Toolbar(
        title = {
            if (!searchMode) {
                BigSizeTextComposable(text = localize(Res.string.history_screen_label))
            } else {
                AppTextField(
                    query = searchQuery,
                    onValueChange = { vm.onSearchQueryChange(it) },
                    onConfirm = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                )
            }
        },
        actions = {
            if (searchMode) {
                AppIconButton(
                    imageVector = Icons.Default.Close,
                    contentDescription = localize(Res.string.close),
                    onClick = {
                        vm.toggleSearchMode()
                        keyboardController?.hide()
                    },
                )
            }
            AppIconButton(
                imageVector = Icons.Default.Search,
                contentDescription = localize(Res.string.search),
                onClick = { vm.toggleSearchMode() },
            )
        },
        navigationIcon = {
            if (searchMode) {
                AppIconButton(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = localize(Res.string.toggle_search_mode_off),
                    onClick = {
                        vm.toggleSearchMode()
                        keyboardController?.hide()
                    }
                )
            }
        },
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}
