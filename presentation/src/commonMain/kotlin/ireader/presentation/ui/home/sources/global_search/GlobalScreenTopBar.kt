package ireader.presentation.ui.home.sources.global_search

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.AppTextField
import ireader.presentation.ui.home.sources.global_search.viewmodel.GlobalSearchScreenState


@OptIn(ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun GlobalScreenTopBar(
        query: String,
        onQueryChange: (String) -> Unit,
        onSearch: (String) -> Unit,
        onPop: () -> Unit,
        onSearchModeChange: (Boolean) -> Unit,
        scrollBehavior: TopAppBarScrollBehavior?
) {

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Toolbar(
        scrollBehavior = scrollBehavior,
        title = {
            AppTextField(
                query = query,
                onValueChange = onQueryChange,
                onConfirm = {
                    onSearch(query)
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
            )
        },
        actions = {
            AppIconButton(
                imageVector = Icons.Default.Search,
                contentDescription = localize(Res.string.search),
                onClick = {
                    onSearchModeChange(true)
                },
            )
        },
        navigationIcon = {
            AppIconButton(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = localize(Res.string.toggle_search_mode_off),
                onClick = {
                    onPop()
                }
            )
        }

    )
}
