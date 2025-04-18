package ireader.presentation.ui.home.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.core.source.HttpSource
import ireader.core.source.model.Filter
import ireader.domain.models.DisplayMode
import ireader.domain.models.getLayoutName
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.components.*
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
        listOf(DisplayMode.ComfortableGrid, DisplayMode.CompactGrid, DisplayMode.List, DisplayMode.OnlyCover)
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
                            contentDescription = localize(MR.strings.close),
                            onClick = {
                                onSearchDisable()
                            },
                    )
                } else if (source?.getFilters()
                                ?.find { it is Filter.Title } != null
                ) {
                    AppIconButton(
                            imageVector = Icons.Default.Search,
                            contentDescription = localize(MR.strings.search),
                            onClick = {
                                onSearchEnable()
                            },
                    )
                }
                if (source is HttpSource) {
                    AppIconButton(
                            imageVector = Icons.Default.Public,
                            contentDescription = localize(MR.strings.webView),
                            onClick = {
                                onWebView()
                            },
                    )
                }
                Box {
                    AppIconButton(
                            imageVector = Icons.Default.GridView,
                            contentDescription = localize(MR.strings.layout),
                            onClick = {
                                topMenu = true
                            },
                            tint = MaterialTheme.colorScheme.onSurface
                    )
                    IDropdownMenu(
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                            expanded = topMenu,
                            onDismissRequest = {
                                topMenu = false
                            }
                    ) {
                        layouts.forEach { layout ->
                            IDropdownMenuItem(onClick = {
                                onLayoutTypeSelect(layout)
                                topMenu = false
                            }, text = {
                                val layoutName = layout.getLayoutName(localizeHelper)
                                val description = when (layout) {
                                    DisplayMode.ComfortableGrid -> localize(MR.strings.comfortable_grid_layout_description)
                                    DisplayMode.CompactGrid -> localize(MR.strings.compact_grid_layout_description)
                                    DisplayMode.List -> localize(MR.strings.list_layout_description) 
                                    DisplayMode.OnlyCover -> localize(MR.strings.cover_only_layout_description)
                                }
                                
                                RadioButton(
                                        text = layoutName,
                                        description = description,
                                        selected = currentLayout == layout,
                                        textColor = MaterialTheme.colorScheme.onSurface,
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        onClick = {
                                            onLayoutTypeSelect(layout)
                                            topMenu = false
                                        }
                                )
                            })
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
