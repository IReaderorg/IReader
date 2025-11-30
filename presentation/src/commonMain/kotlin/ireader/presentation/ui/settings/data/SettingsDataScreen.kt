package ireader.presentation.ui.settings.data

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
 * Enhanced data and storage settings screen following Mihon's comprehensive data management.
 * Provides cache management, data usage settings, and storage optimization.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDataScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    viewModel: SettingsDataViewModel,
    scaffoldPaddingValues: PaddingValues = PaddingValues()
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val listState = rememberSaveable(
        key = "settings_data_scroll_state",
        saver = LazyListState.Saver
    ) {
        LazyListState()
    }

    // Data preferences state
    val imageCacheSize by viewModel.imageCacheSize.collectAsState()
    val chapterCacheSize by viewModel.chapterCacheSize.collectAsState()
    val networkCacheSize by viewModel.networkCacheSize.collectAsState()
    val totalCacheSize by viewModel.totalCacheSize.collectAsState()
    val autoCleanupEnabled by viewModel.autoCleanupEnabled.collectAsState()
    val autoCleanupInterval by viewModel.autoCleanupInterval.collectAsState()
    val maxCacheSize by viewModel.maxCacheSize.collectAsState()
    val clearCacheOnLowStorage by viewModel.clearCacheOnLowStorage.collectAsState()
    val compressImages by viewModel.compressImages.collectAsState()
    val imageQuality by viewModel.imageQuality.collectAsState()
    val preloadNextChapter by viewModel.preloadNextChapter.collectAsState()
    val preloadPreviousChapter by viewModel.preloadPreviousChapter.collectAsState()

    IScaffold(
        modifier = modifier,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = localizeHelper.localize(Res.string.data_storage),
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
            // Storage Usage Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.storage_usage),
                    icon = Icons.Outlined.Storage
                )
            }
            
            item {
                SettingsHighlightCard(
                    title = localizeHelper.localize(Res.string.total_cache_size),
                    description = formatFileSize(totalCacheSize),
                    icon = Icons.Outlined.PieChart,
                    onClick = { viewModel.navigateToStorageBreakdown() }
                )
            }
            
            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.image_cache),
                    description = "Cached book covers and chapter images",
                    icon = Icons.Outlined.Image,
                    onClick = { viewModel.showImageCacheDialog() }
                ) {
                    Text(
                        text = formatFileSize(imageCacheSize),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.chapter_cache),
                    description = "Cached chapter content and metadata",
                    icon = Icons.Outlined.Article,
                    onClick = { viewModel.showChapterCacheDialog() }
                ) {
                    Text(
                        text = formatFileSize(chapterCacheSize),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.network_cache),
                    description = "Cached API responses and web content",
                    icon = Icons.Outlined.Cloud,
                    onClick = { viewModel.showNetworkCacheDialog() }
                ) {
                    Text(
                        text = formatFileSize(networkCacheSize),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Cache Management Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.cache_management),
                    icon = Icons.Outlined.CleaningServices
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.auto_cleanup),
                    description = "Automatically clean old cache files",
                    icon = Icons.Outlined.AutoDelete,
                    checked = autoCleanupEnabled,
                    onCheckedChange = viewModel::setAutoCleanupEnabled
                )
            }
            
            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.cleanup_interval),
                    description = "How often to run automatic cleanup",
                    icon = Icons.Outlined.Schedule,
                    onClick = { viewModel.showCleanupIntervalDialog() },
                    enabled = autoCleanupEnabled
                ) {
                    Text(
                        text = when (autoCleanupInterval) {
                            1 -> "Daily"
                            3 -> "Every 3 days"
                            7 -> "Weekly"
                            14 -> "Every 2 weeks"
                            30 -> "Monthly"
                            else -> "Weekly"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (autoCleanupEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }
            
            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.max_cache_size),
                    description = "Maximum total cache size before cleanup",
                    icon = Icons.Outlined.Storage,
                    onClick = { viewModel.showMaxCacheSizeDialog() }
                ) {
                    Text(
                        text = when (maxCacheSize) {
                            100 -> "100 MB"
                            250 -> "250 MB"
                            500 -> "500 MB"
                            1000 -> "1 GB"
                            2000 -> "2 GB"
                            5000 -> "5 GB"
                            -1 -> "Unlimited"
                            else -> "1 GB"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.clear_cache_on_low_storage),
                    description = "Automatically clear cache when device storage is low",
                    icon = Icons.Outlined.Warning,
                    checked = clearCacheOnLowStorage,
                    onCheckedChange = viewModel::setClearCacheOnLowStorage
                )
            }
            
            // Image Settings Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.image_settings),
                    icon = Icons.Outlined.Image
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.compress_images),
                    description = "Reduce image file sizes to save storage",
                    icon = Icons.Outlined.Compress,
                    checked = compressImages,
                    onCheckedChange = viewModel::setCompressImages
                )
            }
            
            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.image_quality),
                    description = "Balance between quality and file size",
                    icon = Icons.Outlined.HighQuality,
                    onClick = { viewModel.showImageQualityDialog() },
                    enabled = compressImages
                ) {
                    Text(
                        text = when (imageQuality) {
                            25 -> "Low"
                            50 -> "Medium"
                            75 -> "High"
                            90 -> "Very High"
                            100 -> "Original"
                            else -> "High"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (compressImages) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }
            
            // Preloading Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.preloading),
                    icon = Icons.Outlined.Cached
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.preload_next_chapter),
                    description = "Load next chapter in background for faster reading",
                    icon = Icons.Outlined.SkipNext,
                    checked = preloadNextChapter,
                    onCheckedChange = viewModel::setPreloadNextChapter
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.preload_previous_chapter),
                    description = "Keep previous chapter in memory for quick access",
                    icon = Icons.Outlined.SkipPrevious,
                    checked = preloadPreviousChapter,
                    onCheckedChange = viewModel::setPreloadPreviousChapter
                )
            }
            
            // Data Usage Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.data_usage),
                    icon = Icons.Outlined.DataUsage
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.data_usage_statistics),
                    description = "View detailed data usage by feature",
                    icon = Icons.Outlined.Analytics,
                    onClick = { viewModel.navigateToDataUsageStats() }
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.network_settings),
                    description = "Configure network behavior and restrictions",
                    icon = Icons.Outlined.NetworkCheck,
                    onClick = { viewModel.navigateToNetworkSettings() }
                )
            }
            
            // Maintenance Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.maintenance),
                    icon = Icons.Outlined.Build
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.clear_all_cache),
                    description = "Remove all cached data to free up space",
                    icon = Icons.Outlined.ClearAll,
                    onClick = { viewModel.showClearAllCacheDialog() }
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.optimize_database),
                    description = "Compact database and improve performance",
                    icon = Icons.Outlined.Tune,
                    onClick = { viewModel.showOptimizeDatabaseDialog() }
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.reset_data_settings),
                    description = "Reset all data and storage settings to defaults",
                    icon = Icons.Outlined.RestartAlt,
                    onClick = { viewModel.showResetDataSettingsDialog() }
                )
            }
        }
    }
    
    // Cleanup Interval Dialog
    if (viewModel.showCleanupIntervalDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCleanupIntervalDialog() },
            title = { Text(localizeHelper.localize(Res.string.cleanup_interval)) },
            text = {
                Column {
                    val intervals = listOf(
                        1 to "Daily",
                        3 to "Every 3 days",
                        7 to "Weekly",
                        14 to "Every 2 weeks",
                        30 to "Monthly"
                    )
                    intervals.forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = autoCleanupInterval == value,
                                onClick = { 
                                    viewModel.setAutoCleanupInterval(value)
                                    viewModel.dismissCleanupIntervalDialog()
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
                TextButton(onClick = { viewModel.dismissCleanupIntervalDialog() }) {
                    Text(localizeHelper.localize(Res.string.ok))
                }
            }
        )
    }
    
    // Max Cache Size Dialog
    if (viewModel.showMaxCacheSizeDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissMaxCacheSizeDialog() },
            title = { Text(localizeHelper.localize(Res.string.max_cache_size)) },
            text = {
                Column {
                    val sizes = listOf(
                        100 to "100 MB",
                        250 to "250 MB",
                        500 to "500 MB",
                        1000 to "1 GB",
                        2000 to "2 GB",
                        5000 to "5 GB",
                        -1 to "Unlimited"
                    )
                    sizes.forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = maxCacheSize == value,
                                onClick = { 
                                    viewModel.setMaxCacheSize(value)
                                    viewModel.dismissMaxCacheSizeDialog()
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
                TextButton(onClick = { viewModel.dismissMaxCacheSizeDialog() }) {
                    Text(localizeHelper.localize(Res.string.ok))
                }
            }
        )
    }
    
    // Image Quality Dialog
    if (viewModel.showImageQualityDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissImageQualityDialog() },
            title = { Text(localizeHelper.localize(Res.string.image_quality)) },
            text = {
                Column {
                    val qualities = listOf(
                        25 to "Low (Smallest files)",
                        50 to "Medium",
                        75 to "High (Recommended)",
                        90 to "Very High",
                        100 to "Original (Largest files)"
                    )
                    qualities.forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = imageQuality == value,
                                onClick = { 
                                    viewModel.setImageQuality(value)
                                    viewModel.dismissImageQualityDialog()
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
                TextButton(onClick = { viewModel.dismissImageQualityDialog() }) {
                    Text(localizeHelper.localize(Res.string.ok))
                }
            }
        )
    }
    
    // Clear All Cache Confirmation Dialog
    if (viewModel.showClearAllCacheDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClearAllCacheDialog() },
            title = { Text(localizeHelper.localize(Res.string.clear_all_cache)) },
            text = {
                Text(localizeHelper.localize(Res.string.clear_all_cache_confirmation))
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.clearAllCache()
                        viewModel.dismissClearAllCacheDialog()
                    }
                ) {
                    Text(localizeHelper.localize(Res.string.clear_1))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissClearAllCacheDialog() }) {
                    Text(localizeHelper.localize(Res.string.cancel))
                }
            }
        )
    }
    
    // Optimize Database Confirmation Dialog
    if (viewModel.showOptimizeDatabaseDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissOptimizeDatabaseDialog() },
            title = { Text(localizeHelper.localize(Res.string.optimize_database)) },
            text = {
                Text(localizeHelper.localize(Res.string.this_will_compact_the_database))
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.optimizeDatabase()
                        viewModel.dismissOptimizeDatabaseDialog()
                    }
                ) {
                    Text(localizeHelper.localize(Res.string.optimize))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissOptimizeDatabaseDialog() }) {
                    Text(localizeHelper.localize(Res.string.cancel))
                }
            }
        )
    }
    
    // Reset Data Settings Confirmation Dialog
    if (viewModel.showResetDataSettingsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissResetDataSettingsDialog() },
            title = { Text(localizeHelper.localize(Res.string.reset_data_settings)) },
            text = {
                Text(localizeHelper.localize(Res.string.this_will_reset_all_data))
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.resetDataSettings()
                        viewModel.dismissResetDataSettingsDialog()
                    }
                ) {
                    Text(localizeHelper.localize(Res.string.reset))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissResetDataSettingsDialog() }) {
                    Text(localizeHelper.localize(Res.string.cancel))
                }
            }
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.1f KB".format(kb)
    val mb = kb / 1024.0
    if (mb < 1024) return "%.1f MB".format(mb)
    val gb = mb / 1024.0
    return "%.1f GB".format(gb)
}