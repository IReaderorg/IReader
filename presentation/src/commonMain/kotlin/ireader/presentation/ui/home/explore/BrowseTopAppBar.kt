package ireader.presentation.ui.home.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.core.source.HttpSource
import ireader.core.source.LocalSource
import ireader.core.source.model.Filter
import ireader.domain.models.DisplayMode
import ireader.domain.models.getLayoutName
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
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
        scrollBehavior: TopAppBarScrollBehavior?,
        onOpenLocalFolder: (() -> Unit)? = null
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
                AnimatedVisibility(
                    visible = !state.isSearchModeEnable,
                    enter = fadeIn() + scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ),
                    exit = fadeOut() + scaleOut()
                ) {
                    BigSizeTextComposable(text = source?.name ?: "")
                }
                
                AnimatedVisibility(
                    visible = state.isSearchModeEnable,
                    enter = fadeIn() + scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ),
                    exit = fadeOut() + scaleOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
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
                }
            },
            actions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    // Search/Close button with animation
                    AnimatedVisibility(
                        visible = state.isSearchModeEnable,
                        enter = fadeIn() + scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Surface(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            AppIconButton(
                                imageVector = Icons.Default.Close,
                                contentDescription = localize(Res.string.close),
                                onClick = {
                                    onSearchDisable()
                                },
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    AnimatedVisibility(
                        visible = !state.isSearchModeEnable && source?.getFilters()
                            ?.find { it is Filter.Title } != null,
                        enter = fadeIn() + scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Surface(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            AppIconButton(
                                imageVector = Icons.Default.Search,
                                contentDescription = localize(Res.string.search),
                                onClick = {
                                    onSearchEnable()
                                },
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // WebView button
                    if (source is HttpSource) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            AppIconButton(
                                imageVector = Icons.Default.Public,
                                contentDescription = localize(Res.string.webView),
                                onClick = {
                                    onWebView()
                                },
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    
                    // Local Source - Open Folder Button
                    if (source?.id == LocalSource.SOURCE_ID && onOpenLocalFolder != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            AppIconButton(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Open Local Folder",
                                onClick = {
                                    onOpenLocalFolder()
                                },
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    
                    // Layout selector
                    Spacer(modifier = Modifier.width(4.dp))
                    Box {
                        Surface(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            color = if (topMenu) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            AppIconButton(
                                imageVector = Icons.Default.GridView,
                                contentDescription = localize(Res.string.layout),
                                onClick = {
                                    topMenu = true
                                },
                                tint = if (topMenu) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        IDropdownMenu(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .clip(RoundedCornerShape(12.dp)),
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
                                        DisplayMode.ComfortableGrid -> localize(Res.string.comfortable_grid_layout_description)
                                        DisplayMode.CompactGrid -> localize(Res.string.compact_grid_layout_description)
                                        DisplayMode.List -> localize(Res.string.list_layout_description) 
                                        DisplayMode.OnlyCover -> localize(Res.string.cover_only_layout_description)
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
                }
            },
            navigationIcon = {
                TopAppBarBackButton {
                    onPop()
                }
            },
    )
}
