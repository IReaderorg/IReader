package org.ireader.explore

import androidx.compose.foundation.background
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.ireader.common_models.DisplayMode
import org.ireader.common_models.getLayoutName
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.AppTextField
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.components.text_related.RadioButton
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.HttpSource
import org.ireader.core_api.source.model.Filter
import org.ireader.explore.viewmodel.ExploreState
import org.ireader.ui_explore.R

@Composable
fun BrowseTopAppBar(
    state: ExploreState,
    source: CatalogSource?,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSearchDisable: () -> Unit,
    onSearchEnable: () -> Unit,
    onWebView: () -> Unit,
    onPop: () -> Unit,
    onLayoutTypeSelect: (DisplayMode) -> Unit,
    currentLayout: DisplayMode,
) {
    var topMenu by remember {
        mutableStateOf(false)
    }
    val layouts = remember {
        listOf(DisplayMode.ComfortableGrid,DisplayMode.CompactGrid,DisplayMode.List)
    }
    val context = LocalContext.current
    Toolbar(
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
                            text = layout.getLayoutName(context =context ),
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
