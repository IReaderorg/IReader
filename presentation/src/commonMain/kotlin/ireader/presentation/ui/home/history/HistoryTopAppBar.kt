package ireader.presentation.ui.home.history

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
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.AppTextField
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.home.history.viewmodel.HistoryState

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
                BigSizeTextComposable(text = localize(MR.strings.history_screen_label))
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
                    contentDescription = localize(MR.strings.close),
                    onClick = {
                        vm.searchMode = false
                        vm.searchQuery = ""
                        keyboardController?.hide()
                    },
                )
            }
            AppIconButton(
                imageVector = Icons.Default.Search,
                contentDescription = localize(MR.strings.search),
                onClick = {
                    vm.searchMode = true
                },
            )
            // Delete this action bar
//            AppIconButton(
//                imageVector = Icons.Default.Delete,
//                contentDescription = localize(MR.strings.delete_all_histories),
//                onClick = {
//                    onDeleteAll()
//                },
//            )
        },
        navigationIcon = {
            if (vm.searchMode) {
                AppIconButton(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = localize(MR.strings.toggle_search_mode_off),
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
