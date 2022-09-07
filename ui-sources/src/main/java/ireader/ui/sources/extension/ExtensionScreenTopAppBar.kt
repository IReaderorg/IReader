package ireader.ui.sources.extension

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.accompanist.pager.ExperimentalPagerApi
import ireader.ui.component.components.Toolbar
import ireader.ui.component.reusable_composable.AppIconButton
import ireader.ui.component.reusable_composable.AppTextField
import ireader.ui.component.reusable_composable.BigSizeTextComposable
import ireader.ui.sources.R

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
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
                BigSizeTextComposable(text = stringResource(R.string.extensions))
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
                        contentDescription = stringResource(R.string.close),
                        onClick = onClose,
                    )
                } else {
                    AppIconButton(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search),
                        onClick = onSearchEnable,
                    )
                }
                AppIconButton(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.refresh),
                    onClick = onRefresh,
                )
            } else {
                if (searchMode) {
                    AppIconButton(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        onClick = onSearchDisable,
                    )
                } else {
                    AppIconButton(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search),
                        onClick = onSearchEnable,
                    )
                    AppIconButton(
                        imageVector = Icons.Default.TravelExplore,
                        contentDescription = stringResource(R.string.search),
                        onClick = onSearchNavigate,
                    )
                }
            }
        },
        navigationIcon = {
            if (searchMode) {

                AppIconButton(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.toggle_search_mode_off),
                    onClick = onSearchDisable
                )
            } else null
        },

    )
}
