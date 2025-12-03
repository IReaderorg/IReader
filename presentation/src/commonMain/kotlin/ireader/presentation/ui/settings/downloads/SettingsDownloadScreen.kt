package ireader.presentation.ui.settings.downloads

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.components.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.Res

/**
 * Enhanced download settings screen following Mihon's comprehensive download management.
 * Provides category exclusions, automatic download settings, and storage management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDownloadScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    viewModel: SettingsDownloadViewModel,
    scaffoldPaddingValues: PaddingValues = PaddingValues()
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val listState = rememberSaveable(
        key = "settings_download_scroll_state",
        saver = LazyListState.Saver
    ) {
        LazyListState()
    }

    // Download preferences state
    val downloadLocation by viewModel.downloadLocation.collectAsState()
    val downloadOnlyOverWifi by viewModel.downloadOnlyOverWifi.collectAsState()
    val downloadNewChapters by viewModel.downloadNewChapters.collectAsState()
    val downloadNewChaptersCategories by viewModel.downloadNewChaptersCategories.collectAsState()
    val autoDeleteChapters by viewModel.autoDeleteChapters.collectAsState()
    val removeAfterReading by viewModel.removeAfterReading.collectAsState()
    val removeAfterMarkedAsRead by viewModel.removeAfterMarkedAsRead.collectAsState()
    val removeExcludeCategories by viewModel.removeExcludeCategories.collectAsState()
    val saveChaptersAsCBZ by viewModel.saveChaptersAsCBZ.collectAsState()
    val splitTallImages by viewModel.splitTallImages.collectAsState()

    IScaffold(
        modifier = modifier,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = localizeHelper.localize(Res.string.downloads),
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
            // Storage Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.storage),
                    icon = Icons.Outlined.Storage
                )
            }
            
            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.download_location),
                    description = "Where downloaded chapters are stored",
                    icon = Icons.Outlined.Folder,
                    onClick = { viewModel.showDownloadLocationDialog() }
                ) {
                    Text(
                        text = downloadLocation.substringAfterLast("/").ifEmpty { "Default" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.storage_usage),
                    description = "View and manage downloaded content",
                    icon = Icons.Outlined.PieChart,
                    onClick = { viewModel.navigateToStorageUsage() }
                )
            }
            
            // Download Behavior Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.download_behavior),
                    icon = Icons.Outlined.Download
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.download_only_over_wifi),
                    description = "Restrict downloads to WiFi connections only",
                    icon = Icons.Outlined.Wifi,
                    checked = downloadOnlyOverWifi,
                    onCheckedChange = viewModel::setDownloadOnlyOverWifi
                )
            }
            // Automatic Downloads Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.automatic_downloads),
                    icon = Icons.Outlined.AutoMode
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.download_new_chapters),
                    description = "Automatically download newly detected chapters",
                    icon = Icons.Outlined.NewReleases,
                    checked = downloadNewChapters,
                    onCheckedChange = viewModel::setDownloadNewChapters
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.download_categories),
                    description = "Select which categories to auto-download",
                    icon = Icons.Outlined.Category,
                    onClick = { viewModel.showDownloadCategoriesDialog() },
                    enabled = downloadNewChapters
                )
            }
            
            // Auto Delete Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.auto_delete),
                    icon = Icons.Outlined.DeleteSweep
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.auto_delete_chapters),
                    description = "Automatically remove downloaded chapters",
                    icon = Icons.Outlined.DeleteSweep,
                    checked = autoDeleteChapters,
                    onCheckedChange = viewModel::setAutoDeleteChapters
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.remove_after_reading),
                    description = "Delete chapters after reading them",
                    icon = Icons.Outlined.RemoveRedEye,
                    checked = removeAfterReading,
                    onCheckedChange = viewModel::setRemoveAfterReading,
                    enabled = autoDeleteChapters
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.remove_after_marked_as_read),
                    description = "Delete chapters when marked as read",
                    icon = Icons.Outlined.DoneAll,
                    checked = removeAfterMarkedAsRead,
                    onCheckedChange = viewModel::setRemoveAfterMarkedAsRead,
                    enabled = autoDeleteChapters
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.exclude_categories),
                    description = "Categories to exclude from auto-deletion",
                    icon = Icons.Outlined.Block,
                    onClick = { viewModel.showRemoveExcludeCategoriesDialog() },
                    enabled = autoDeleteChapters
                )
            }
            
            // File Format Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.file_format),
                    icon = Icons.Outlined.Archive
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.save_chapters_as_cbz),
                    description = "Save downloaded chapters in CBZ format",
                    icon = Icons.Outlined.Archive,
                    checked = saveChaptersAsCBZ,
                    onCheckedChange = viewModel::setSaveChaptersAsCBZ
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.split_tall_images),
                    description = "Split very tall images into multiple pages",
                    icon = Icons.Outlined.CallSplit,
                    checked = splitTallImages,
                    onCheckedChange = viewModel::setSplitTallImages
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
                    title = localizeHelper.localize(Res.string.download_queue),
                    description = "Manage current download queue",
                    icon = Icons.Outlined.Queue,
                    onClick = { viewModel.navigateToDownloadQueue() }
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.clear_download_cache),
                    description = "Remove temporary download files",
                    icon = Icons.Outlined.ClearAll,
                    onClick = { viewModel.showClearCacheDialog() }
                )
            }
        }
    }
    
    // Download Location Dialog
    if (viewModel.showDownloadLocationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDownloadLocationDialog() },
            title = { Text(localizeHelper.localize(Res.string.download_location)) },
            text = {
                Column {
                    Text(
                        text = localizeHelper.localize(Res.string.current_location),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = downloadLocation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Button(
                        onClick = { viewModel.selectDownloadLocation() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(localizeHelper.localize(Res.string.choose_folder))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissDownloadLocationDialog() }) {
                    Text(localizeHelper.localize(Res.string.ok))
                }
            }
        )
    }

    
    // Clear Cache Confirmation Dialog
    if (viewModel.showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClearCacheDialog() },
            title = { Text(localizeHelper.localize(Res.string.clear_download_cache)) },
            text = {
                Text(localizeHelper.localize(Res.string.clear_download_cache_confirmation))
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.clearDownloadCache()
                        viewModel.dismissClearCacheDialog()
                    }
                ) {
                    Text(localizeHelper.localize(Res.string.clear_1))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissClearCacheDialog() }) {
                    Text(localizeHelper.localize(Res.string.cancel))
                }
            }
        )
    }
}
