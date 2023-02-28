package ireader.presentation.ui.home.explore

import androidx.compose.foundation.background
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.core.source.HttpSource
import ireader.core.source.model.Filter
import ireader.domain.models.DisplayMode
import ireader.domain.models.getLayoutName
import ireader.presentation.R
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.AppTextField
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.component.text_related.RadioButton
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.home.explore.viewmodel.ExploreState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseTopAppBar(
    state: ExploreState,
    source: ireader.core.source.CatalogSource?,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSearchDisable: () -> Unit,
    onSearchEnable: () -> Unit,
    onWebView: () -> Unit,
    onPop: () -> Unit,
    onLayoutTypeSelect: (DisplayMode) -> Unit,
    currentLayout: DisplayMode,
    scrollBehavior: TopAppBarScrollBehavior?
) {
    var topMenu by remember {
        mutableStateOf(false)
    }
    val layouts = remember {
        listOf(DisplayMode.ComfortableGrid, DisplayMode.CompactGrid, DisplayMode.List,DisplayMode.OnlyCover)
    }
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    Toolbar(
        scrollBehavior = scrollBehavior,
        title = {
            if (!state.isSearchModeEnable) {
                BigSizeTextComposable(text = source?.name ?: "")
            } else {
                AppTextField(
                    query = state.searchQuery ?: "",
                    onValueChange = {
                        onValueChange(it)
                    },
                    onConfirm = {
                        onSearch()
                    },
                )
            }
        },
        actions = {
            if (state.isSearchModeEnable) {
                AppIconButton(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close),
                    onClick = {
                        onSearchDisable()
                    },
                )
            } else if (source?.getFilters()
                ?.find { it is Filter.Title } != null
            ) {
                AppIconButton(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search),
                    onClick = {
                        onSearchEnable()
                    },
                )
            }
            if (source is HttpSource) {
                AppIconButton(
                    imageVector = Icons.Default.Public,
                    contentDescription = stringResource(R.string.webView),
                    onClick = {
                        onWebView()
                    },
                )
            }
            AppIconButton(
                imageVector = Icons.Default.GridView,
                contentDescription = stringResource(R.string.layout),
                onClick = {
                    topMenu = true
                },
            )
            DropdownMenu(
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                expanded = topMenu,
                onDismissRequest = {
                    topMenu = false
                }
            ) {
                layouts.forEach { layout ->
                    DropdownMenuItem(onClick = {
                        onLayoutTypeSelect(layout)
                        topMenu = false
                    }) {
                        RadioButton(
                            text = layout.getLayoutName(localizeHelper),
                            selected = currentLayout == layout,
                            onClick = {
                                onLayoutTypeSelect(layout)
                                topMenu = false
                            }
                        )
                    }
                }
            }
        },
        navigationIcon = {
            TopAppBarBackButton {
                onPop()
            }
        },
    )
}
