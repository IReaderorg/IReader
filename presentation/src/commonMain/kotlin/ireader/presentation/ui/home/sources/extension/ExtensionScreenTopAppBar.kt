package ireader.presentation.ui.home.sources.extension

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
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
    onMigrate: (() -> Unit)? = null,
    onAddRepository: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Toolbar(
        scrollBehavior = scrollBehavior,
        title = {
            if (!searchMode) {
                BigSizeTextComposable(text = localize(Res.string.extensions))
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
                        contentDescription = localize(Res.string.close),
                        onClick = onClose,
                    )
                } else {
                    AppIconButton(
                        imageVector = Icons.Default.Search,
                        contentDescription = localize(Res.string.search),
                        onClick = onSearchEnable,
                    )
                }
                AppIconButton(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = localize(Res.string.refresh),
                    onClick = onRefresh,
                )
                if (onAddRepository != null) {
                    AppIconButton(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Repository",
                        onClick = onAddRepository,
                    )
                }
            } else {
                if (searchMode) {
                    AppIconButton(
                        imageVector = Icons.Default.Close,
                        contentDescription = localize(Res.string.close),
                        onClick = onSearchDisable,
                    )
                } else {
                    AppIconButton(
                        imageVector = Icons.Default.Search,
                        contentDescription = localize(Res.string.search),
                        onClick = onSearchEnable,
                    )
                    AppIconButton(
                        imageVector = Icons.Default.TravelExplore,
                        contentDescription = localize(Res.string.search),
                        onClick = onSearchNavigate,
                    )
                    if (onMigrate != null) {
                        AppIconButton(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Migrate from Source",
                            onClick = onMigrate,
                        )
                    }
                }
            }
        },
        navigationIcon = {
            if (searchMode) {

                AppIconButton(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = localize(Res.string.toggle_search_mode_off),
                    onClick = onSearchDisable
                )
            } else null
        },

    )
}
