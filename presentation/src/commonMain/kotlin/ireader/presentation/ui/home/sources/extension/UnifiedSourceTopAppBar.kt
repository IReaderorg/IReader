package ireader.presentation.ui.home.sources.extension

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ireader.i18n.localize
import ireader.i18n.resources.*
import ireader.i18n.resources.add_repository
import ireader.i18n.resources.browse_settings
import ireader.i18n.resources.close
import ireader.i18n.resources.migrate_from_source
import ireader.i18n.resources.refresh
import ireader.i18n.resources.search
import ireader.i18n.resources.sources
import ireader.i18n.resources.toggle_search_mode_off
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.AppTextField
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Unified top app bar for the merged Sources screen
 * Clean design with overflow menu for less common actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedSourceTopAppBar(
    searchMode: Boolean,
    query: String,
    onValueChange: (query: String) -> Unit,
    onConfirm: () -> Unit,
    onSearchDisable: () -> Unit,
    onSearchEnable: () -> Unit,
    onRefresh: () -> Unit,
    onSearchNavigate: () -> Unit,
    onMigrate: (() -> Unit)? = null,
    onAddRepository: (() -> Unit)? = null,
    onBrowseSettings: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var showOverflowMenu by remember { mutableStateOf(false) }
    
    Toolbar(
        scrollBehavior = scrollBehavior,
        title = {
            if (!searchMode) {
                BigSizeTextComposable(text = localize(Res.string.sources))
            } else {
                AppTextField(
                    query = query,
                    onValueChange = onValueChange,
                    onConfirm = onConfirm,
                )
            }
        },
        actions = {
            if (searchMode) {
                AppIconButton(
                    imageVector = Icons.Default.Close,
                    contentDescription = localize(Res.string.close),
                    onClick = onSearchDisable,
                )
            } else {
                // Search - primary action
                AppIconButton(
                    imageVector = Icons.Default.Search,
                    contentDescription = localize(Res.string.search),
                    onClick = onSearchEnable,
                )
                
                // Refresh - primary action
                AppIconButton(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = localize(Res.string.refresh),
                    onClick = onRefresh,
                )
                
                // Overflow menu for less common actions
                AppIconButton(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = localizeHelper.localize(Res.string.more_options_1),
                    onClick = { showOverflowMenu = true },
                )
                
                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { showOverflowMenu = false }
                ) {
                    // Global Search
                    DropdownMenuItem(
                        text = { Text(localizeHelper.localize(Res.string.global_search)) },
                        onClick = {
                            showOverflowMenu = false
                            onSearchNavigate()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.TravelExplore,
                                contentDescription = null
                            )
                        }
                    )
                    
                    // Add Repository
                    if (onAddRepository != null) {
                        DropdownMenuItem(
                            text = { Text(localizeHelper.localize(Res.string.add_repository)) },
                            onClick = {
                                showOverflowMenu = false
                                onAddRepository()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                    
                    // Browse Settings
                    if (onBrowseSettings != null) {
                        DropdownMenuItem(
                            text = { Text(localizeHelper.localize(Res.string.browse_settings)) },
                            onClick = {
                                showOverflowMenu = false
                                onBrowseSettings()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                    
                    // Migrate
                    if (onMigrate != null) {
                        DropdownMenuItem(
                            text = { Text(localizeHelper.localize(Res.string.migrate_from_source)) },
                            onClick = {
                                showOverflowMenu = false
                                onMigrate()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null
                                )
                            }
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
