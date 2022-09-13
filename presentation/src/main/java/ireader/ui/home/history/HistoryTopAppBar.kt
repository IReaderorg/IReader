package ireader.ui.home.history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import ireader.ui.component.components.Toolbar
import ireader.ui.component.reusable_composable.AppIconButton
import ireader.ui.component.reusable_composable.AppTextField
import ireader.ui.component.reusable_composable.BigSizeTextComposable
import ireader.ui.home.history.viewmodel.HistoryState
import ireader.presentation.R

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryTopAppBar(
    modifier: Modifier = Modifier,
    vm: HistoryState,
    onDeleteAll: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Toolbar(
        title = {
            if (!vm.searchMode) {
                BigSizeTextComposable(text = stringResource(R.string.history_screen_label))
            } else {
                AppTextField(
                    query = vm.searchQuery,
                    onValueChange = {
                        vm.searchQuery = it
                    },
                    onConfirm = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                )
            }
        },
        actions = {
            if (vm.searchMode) {
                AppIconButton(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close),
                    onClick = {
                        vm.searchMode = false
                        vm.searchQuery = ""
                        keyboardController?.hide()
                    },
                )
            }
            AppIconButton(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search),
                onClick = {
                    vm.searchMode = true
                },
            )
            AppIconButton(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete_all_histories),
                onClick = {
                    onDeleteAll()
                },
            )
        },
        navigationIcon = {
            if (vm.searchMode) {
                AppIconButton(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.toggle_search_mode_off),
                    onClick = {
                        vm.searchMode = false
                        vm.searchQuery = ""
                        keyboardController?.hide()
                    }
                )
            }
        },
        scrollBehavior = scrollBehavior,
        modifier = modifier

    )
}
