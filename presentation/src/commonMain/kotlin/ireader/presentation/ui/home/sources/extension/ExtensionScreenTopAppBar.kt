package ireader.presentation.ui.home.sources.extension

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import ireader.i18n.localize

import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.AppTextField
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionScreenTopAppBar(
    currentPage: Int,
    searchMode: Boolean,
    query: String,
    onValueChange: (query: String) -> Unit,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
    onSearchDisable: () -> Unit,
    onSearchEnable: () -> Unit,
    onRefresh: () -> Unit,
    onSearchNavigate: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Toolbar(
        scrollBehavior = scrollBehavior,
        title = {
            if (!searchMode) {
                BigSizeTextComposable(text = localize { xml -> xml.extensions })
            } else {
                AppTextField(
                    query = query,
                    onValueChange = onValueChange,
                    onConfirm = onConfirm,
                )
            }
        },
        actions = {
            if (currentPage == 1) {
                if (searchMode) {
                    AppIconButton(
                        imageVector = Icons.Default.Close,
                        contentDescription = localize() { xml ->
                            xml.close
                        },
                        onClick = onClose,
                    )
                } else {
                    AppIconButton(
                        imageVector = Icons.Default.Search,
                        contentDescription = localize { xml -> xml.search },
                        onClick = onSearchEnable,
                    )
                }
                AppIconButton(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = localize { xml -> xml.refresh },
                    onClick = onRefresh,
                )
            } else {
                if (searchMode) {
                    AppIconButton(
                        imageVector = Icons.Default.Close,
                        contentDescription = localize() { xml ->
                            xml.close
                        },
                        onClick = onSearchDisable,
                    )
                } else {
                    AppIconButton(
                        imageVector = Icons.Default.Search,
                        contentDescription = localize { xml -> xml.search },
                        onClick = onSearchEnable,
                    )
                    AppIconButton(
                        imageVector = Icons.Default.TravelExplore,
                        contentDescription = localize { xml -> xml.search },
                        onClick = onSearchNavigate,
                    )
                }
            }
        },
        navigationIcon = {
            if (searchMode) {

                AppIconButton(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = localize { xml ->
                        xml.toggleSearchModeOff
                    },
                    onClick = onSearchDisable
                )
            } else null
        },

    )
}
