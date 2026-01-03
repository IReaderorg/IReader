package ireader.presentation.ui.settings.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.components.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Enhanced library settings screen following Mihon's LibrarySettingsScreenModel.
 * Provides comprehensive library management with sorting, filtering, and update preferences.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsLibraryScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    viewModel: SettingsLibraryViewModel,
    scaffoldPaddingValues: PaddingValues = PaddingValues()
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val listState = rememberSaveable(
        key = "settings_library_scroll_state",
        saver = LazyListState.Saver
    ) {
        LazyListState()
    }

    // Library preferences state
    val defaultSort by viewModel.defaultSort.collectAsState()
    val defaultSortDirection by viewModel.defaultSortDirection.collectAsState()
    val showContinueReadingButton by viewModel.showContinueReadingButton.collectAsState()
    val showUnreadBadge by viewModel.showUnreadBadge.collectAsState()
    val showDownloadBadge by viewModel.showDownloadBadge.collectAsState()
    val showLanguageBadge by viewModel.showLanguageBadge.collectAsState()
    val showLocalBadge by viewModel.showLocalBadge.collectAsState()
    val autoUpdateLibrary by viewModel.autoUpdateLibrary.collectAsState()
    val autoUpdateInterval by viewModel.autoUpdateInterval.collectAsState()
    val autoUpdateRestrictions by viewModel.autoUpdateRestrictions.collectAsState()
    val updateOnlyCompleted by viewModel.updateOnlyCompleted.collectAsState()
    val updateOnlyNonCompleted by viewModel.updateOnlyNonCompleted.collectAsState()
    val skipTitlesWithoutChapters by viewModel.skipTitlesWithoutChapters.collectAsState()
    val refreshCoversToo by viewModel.refreshCoversToo.collectAsState()
    
    // Default category state
    val defaultCategory by viewModel.defaultCategory.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()

    IScaffold(
        modifier = modifier,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = localizeHelper.localize(Res.string.library),
                popBackStack = onNavigateUp,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            )
        ) {
            // Display Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.display),
                    icon = Icons.Outlined.ViewList
                )
            }
            
            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.default_sort),
                    description = "Default sorting method for library",
                    icon = Icons.Outlined.Sort,
                    onClick = { viewModel.showDefaultSortDialog() }
                ) {
                    Text(
                        text = when (defaultSort) {
                            "alphabetical" -> "Alphabetical"
                            "last_read" -> "Last Read"
                            "last_updated" -> "Last Updated"
                            "unread_count" -> "Unread Count"
                            "total_chapters" -> "Total Chapters"
                            "date_added" -> "Date Added"
                            else -> "Alphabetical"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.sort_direction),
                    description = "Ascending or descending order",
                    icon = Icons.Outlined.SwapVert,
                    onClick = { viewModel.toggleSortDirection() }
                ) {
                    Text(
                        text = if (defaultSortDirection) "Ascending" else "Descending",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.continue_reading_button),
                    description = "Show continue reading button for in-progress books",
                    icon = Icons.Outlined.PlayArrow,
                    checked = showContinueReadingButton,
                    onCheckedChange = viewModel::setShowContinueReadingButton
                )
            }
            
            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.default_category),
                    description = "Category to assign when adding books to library",
                    icon = Icons.Outlined.Category,
                    onClick = { viewModel.showDefaultCategoryDialog() }
                ) {
                    Text(
                        text = viewModel.getCategoryDisplayName(defaultCategory),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Badges Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.badges),
                    icon = Icons.Outlined.Label
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.unread_badge),
                    description = "Show unread chapter count on library items",
                    icon = Icons.Outlined.FiberNew,
                    checked = showUnreadBadge,
                    onCheckedChange = viewModel::setShowUnreadBadge
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.download_badge),
                    description = "Show download status on library items",
                    icon = Icons.Outlined.Download,
                    checked = showDownloadBadge,
                    onCheckedChange = viewModel::setShowDownloadBadge
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.language_badge),
                    description = "Show source language on library items",
                    icon = Icons.Outlined.Language,
                    checked = showLanguageBadge,
                    onCheckedChange = viewModel::setShowLanguageBadge
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.local_badge),
                    description = "Show local source indicator on library items",
                    icon = Icons.Outlined.Storage,
                    checked = showLocalBadge,
                    onCheckedChange = viewModel::setShowLocalBadge
                )
            }
            
            // Auto Update Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.auto_update_1),
                    icon = Icons.Outlined.Update
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.auto_update_library),
                    description = "Automatically check for new chapters",
                    icon = Icons.Outlined.Update,
                    checked = autoUpdateLibrary,
                    onCheckedChange = viewModel::setAutoUpdateLibrary
                )
            }
            
            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.update_interval),
                    description = "How often to check for updates",
                    icon = Icons.Outlined.Schedule,
                    onClick = { viewModel.showUpdateIntervalDialog() },
                    enabled = autoUpdateLibrary
                ) {
                    Text(
                        text = when (autoUpdateInterval) {
                            1 -> "1 hour"
                            2 -> "2 hours"
                            3 -> "3 hours"
                            6 -> "6 hours"
                            12 -> "12 hours"
                            24 -> "Daily"
                            48 -> "Every 2 days"
                            168 -> "Weekly"
                            else -> "12 hours"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (autoUpdateLibrary) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }
            
            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.update_restrictions),
                    description = "When automatic updates are allowed",
                    icon = Icons.Outlined.WifiOff,
                    onClick = { viewModel.showUpdateRestrictionsDialog() },
                    enabled = autoUpdateLibrary
                ) {
                    Text(
                        text = when {
                            autoUpdateRestrictions.contains("wifi") && autoUpdateRestrictions.contains("charging") -> "WiFi + Charging"
                            autoUpdateRestrictions.contains("wifi") -> "WiFi Only"
                            autoUpdateRestrictions.contains("charging") -> "Charging Only"
                            else -> "None"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (autoUpdateLibrary) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }
            
            // Update Filters Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.update_filters),
                    icon = Icons.Outlined.FilterList
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.update_only_completed),
                    description = "Only update books marked as completed",
                    icon = Icons.Outlined.CheckCircle,
                    checked = updateOnlyCompleted,
                    onCheckedChange = viewModel::setUpdateOnlyCompleted,
                    enabled = autoUpdateLibrary
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.update_only_non_completed),
                    description = "Only update books not marked as completed",
                    icon = Icons.Outlined.RadioButtonUnchecked,
                    checked = updateOnlyNonCompleted,
                    onCheckedChange = viewModel::setUpdateOnlyNonCompleted,
                    enabled = autoUpdateLibrary && !updateOnlyCompleted
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.skip_titles_without_chapters),
                    description = "Don't update books that have no chapters",
                    icon = Icons.Outlined.SkipNext,
                    checked = skipTitlesWithoutChapters,
                    onCheckedChange = viewModel::setSkipTitlesWithoutChapters,
                    enabled = autoUpdateLibrary
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.refresh_covers_too),
                    description = "Also update book covers during library updates",
                    icon = Icons.Outlined.Image,
                    checked = refreshCoversToo,
                    onCheckedChange = viewModel::setRefreshCoversToo,
                    enabled = autoUpdateLibrary
                )
            }
            
            // Advanced Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.advanced),
                    icon = Icons.Outlined.Tune
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.category_management),
                    description = "Create and organize book categories",
                    icon = Icons.Outlined.Category,
                    onClick = { viewModel.navigateToCategoryManagement() }
                )
            }
        }
    }
    
    // Default Sort Dialog
    if (viewModel.showDefaultSortDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDefaultSortDialog() },
            title = { Text(localizeHelper.localize(Res.string.default_sort)) },
            text = {
                Column {
                    val sortOptions = listOf(
                        "alphabetical" to "Alphabetical",
                        "last_read" to "Last Read",
                        "last_updated" to "Last Updated",
                        "unread_count" to "Unread Count",
                        "total_chapters" to "Total Chapters",
                        "date_added" to "Date Added"
                    )
                    sortOptions.forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = defaultSort == value,
                                onClick = { 
                                    viewModel.setDefaultSort(value)
                                    viewModel.dismissDefaultSortDialog()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissDefaultSortDialog() }) {
                    Text(localizeHelper.localize(Res.string.ok))
                }
            }
        )
    }
    
    // Update Interval Dialog
    if (viewModel.showUpdateIntervalDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissUpdateIntervalDialog() },
            title = { Text(localizeHelper.localize(Res.string.update_interval)) },
            text = {
                Column {
                    val intervals = listOf(
                        1 to "1 hour",
                        2 to "2 hours",
                        3 to "3 hours",
                        6 to "6 hours",
                        12 to "12 hours",
                        24 to "Daily",
                        48 to "Every 2 days",
                        168 to "Weekly"
                    )
                    intervals.forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = autoUpdateInterval == value,
                                onClick = { 
                                    viewModel.setAutoUpdateInterval(value)
                                    viewModel.dismissUpdateIntervalDialog()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissUpdateIntervalDialog() }) {
                    Text(localizeHelper.localize(Res.string.ok))
                }
            }
        )
    }
    
    // Update Restrictions Dialog
    if (viewModel.showUpdateRestrictionsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissUpdateRestrictionsDialog() },
            title = { Text(localizeHelper.localize(Res.string.update_restrictions)) },
            text = {
                Column {
                    Text(
                        text = localizeHelper.localize(Res.string.select_when_automatic_updates_are_allowed),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = autoUpdateRestrictions.contains("wifi"),
                            onCheckedChange = { viewModel.toggleUpdateRestriction("wifi") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = localizeHelper.localize(Res.string.wifi_only),
                            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = autoUpdateRestrictions.contains("charging"),
                            onCheckedChange = { viewModel.toggleUpdateRestriction("charging") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = localizeHelper.localize(Res.string.while_charging),
                            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissUpdateRestrictionsDialog() }) {
                    Text(localizeHelper.localize(Res.string.ok))
                }
            }
        )
    }
    
    // Default Category Dialog
    if (viewModel.showDefaultCategoryDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDefaultCategoryDialog() },
            title = { Text(localizeHelper.localize(Res.string.default_category)) },
            text = {
                Column {
                    // "Always ask" option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = defaultCategory == ireader.domain.models.entities.Category.UNCATEGORIZED_ID,
                            onClick = { 
                                viewModel.setDefaultCategory(ireader.domain.models.entities.Category.UNCATEGORIZED_ID)
                                viewModel.dismissDefaultCategoryDialog()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Always ask",
                            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                        )
                    }
                    
                    // "Default (no category)" option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = defaultCategory == ireader.domain.models.entities.Category.ALL_ID,
                            onClick = { 
                                viewModel.setDefaultCategory(ireader.domain.models.entities.Category.ALL_ID)
                                viewModel.dismissDefaultCategoryDialog()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Default (no category)",
                            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                        )
                    }
                    
                    // User categories
                    availableCategories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = defaultCategory == category.id,
                                onClick = { 
                                    viewModel.setDefaultCategory(category.id)
                                    viewModel.dismissDefaultCategoryDialog()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category.name,
                                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissDefaultCategoryDialog() }) {
                    Text(localizeHelper.localize(Res.string.ok))
                }
            }
        )
    }
}
