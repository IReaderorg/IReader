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
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.model.Filter
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.LayoutType
import org.ireader.domain.models.layouts
import org.ireader.presentation.feature_explore.presentation.browse.viewmodel.ExploreState
import org.ireader.presentation.feature_library.presentation.components.RadioButtonWithTitleComposable
import org.ireader.presentation.presentation.Toolbar
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.AppTextField
import org.ireader.presentation.presentation.reusable_composable.BigSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarBackButton

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
    Toolbar(
        title = {
            if (!state.isSearchModeEnable) {
                BigSizeTextComposable(text = source.name)
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
        backgroundColor = MaterialTheme.colors.background,
        actions = {
            if (state.isSearchModeEnable) {
                AppIconButton(
                    imageVector = Icons.Default.Close,
                    title = "Close",
                    onClick = {
                        onSearchDisable()
                    },
                )
            } else if (source.getFilters()
                    .find { it is Filter.Title } != null
            ) {
                AppIconButton(
                    imageVector = Icons.Default.Search,
                    title = "Search",
                    onClick = {
                        onSearchEnable()
                    },
                )
            }
            AppIconButton(
                imageVector = Icons.Default.Public,
                title = "WebView",
                onClick = {
                    onWebView()
                },
            )
            AppIconButton(
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

