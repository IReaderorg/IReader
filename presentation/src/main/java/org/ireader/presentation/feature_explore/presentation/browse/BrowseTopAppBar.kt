package org.ireader.presentation.feature_explore.presentation.browse

import androidx.compose.foundation.background
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.LayoutType
import org.ireader.domain.models.layouts
import org.ireader.domain.view_models.explore.ExploreState
import org.ireader.presentation.feature_library.presentation.components.RadioButtonWithTitleComposable
import org.ireader.presentation.presentation.ToolBar
import org.ireader.presentation.presentation.reusable_composable.TopAppBarActionButton
import org.ireader.presentation.presentation.reusable_composable.TopAppBarBackButton
import org.ireader.presentation.presentation.reusable_composable.TopAppBarSearch
import org.ireader.presentation.presentation.reusable_composable.TopAppBarTitle
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Filter

@Composable
fun BrowseTopAppBar(
    state: ExploreState,
    source: CatalogSource,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSearchDisable: () -> Unit,
    onSearchEnable: () -> Unit,
    onWebView: () -> Unit,
    onPop: () -> Unit,
    onLayoutTypeSelect: (DisplayMode) -> Unit,
    currentLayout: LayoutType,
) {
    var topMenu by remember {
        mutableStateOf(false)
    }
    ToolBar(
        title = {
            if (!state.isSearchModeEnable) {
                TopAppBarTitle(title = source.name)
            } else {
                TopAppBarSearch(query = state.searchQuery,
                    onValueChange = {
                        onValueChange(it)
                    },
                    onSearch = {
                        onSearch()
                    },
                    isSearchModeEnable = state.searchQuery.isNotBlank())
            }
        },
        backgroundColor = MaterialTheme.colors.background,
        actions = {
            if (state.isSearchModeEnable) {
                TopAppBarActionButton(
                    imageVector = Icons.Default.Close,
                    title = "Close",
                    onClick = {
                        onSearchDisable()
                    },
                )
            } else if (source.getFilters()
                    .find { it is Filter.Title } != null
            ) {
                TopAppBarActionButton(
                    imageVector = Icons.Default.Search,
                    title = "Search",
                    onClick = {
                        onSearchEnable()
                    },
                )
            }
            TopAppBarActionButton(
                imageVector = Icons.Default.Public,
                title = "WebView",
                onClick = {
                    onWebView()
                },
            )
            TopAppBarActionButton(
                imageVector = Icons.Default.GridView,
                title = "Layout",
                onClick = {
                    topMenu = true
                },
            )
            DropdownMenu(
                modifier = Modifier.background(MaterialTheme.colors.background),
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
                        RadioButtonWithTitleComposable(
                            text = layout.title,
                            selected = currentLayout == layout.layout,
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

