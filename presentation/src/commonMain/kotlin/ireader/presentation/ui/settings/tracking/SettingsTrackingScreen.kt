package ireader.presentation.ui.settings.tracking

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
 * Enhanced tracking settings screen following Mihon's TrackerManager system.
 * Provides external service integration for MyAnimeList, AniList, Kitsu, and MangaUpdates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTrackingScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    viewModel: SettingsTrackingViewModel,
    scaffoldPaddingValues: PaddingValues = PaddingValues()
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val listState = rememberSaveable(
        key = "settings_tracking_scroll_state",
        saver = LazyListState.Saver
    ) {
        LazyListState()
    }

    // Tracking service states
    val malEnabled by viewModel.malEnabled.collectAsState()
    val malLoggedIn by viewModel.malLoggedIn.collectAsState()
    val aniListEnabled by viewModel.aniListEnabled.collectAsState()
    val aniListLoggedIn by viewModel.aniListLoggedIn.collectAsState()
    val kitsuEnabled by viewModel.kitsuEnabled.collectAsState()
    val kitsuLoggedIn by viewModel.kitsuLoggedIn.collectAsState()
    val mangaUpdatesEnabled by viewModel.mangaUpdatesEnabled.collectAsState()
    val mangaUpdatesLoggedIn by viewModel.mangaUpdatesLoggedIn.collectAsState()
    
    // Auto-sync preferences
    val autoSyncEnabled by viewModel.autoSyncEnabled.collectAsState()
    val autoSyncInterval by viewModel.autoSyncInterval.collectAsState()
    val syncOnlyOverWifi by viewModel.syncOnlyOverWifi.collectAsState()
    val autoUpdateStatus by viewModel.autoUpdateStatus.collectAsState()
    val autoUpdateProgress by viewModel.autoUpdateProgress.collectAsState()
    val autoUpdateScore by viewModel.autoUpdateScore.collectAsState()

    IScaffold(
        modifier = modifier,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = localizeHelper.localize(Res.string.tracking),
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
            // Tracking Services Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.tracking_services),
                    icon = Icons.Outlined.Sync
                )
            }
            
            // MyAnimeList
            item {
                TrackingServiceItem(
                    serviceName = "MyAnimeList",
                    serviceIcon = Icons.Outlined.Star, // TODO: Use actual MAL icon
                    enabled = malEnabled,
                    loggedIn = malLoggedIn,
                    onToggleEnabled = viewModel::setMalEnabled,
                    onLogin = { viewModel.loginToMal() },
                    onLogout = { viewModel.logoutFromMal() },
                    onConfigure = { viewModel.configureMal() }
                )
            }
            
            // AniList
            item {
                TrackingServiceItem(
                    serviceName = "AniList",
                    serviceIcon = Icons.Outlined.Favorite, // TODO: Use actual AniList icon
                    enabled = aniListEnabled,
                    loggedIn = aniListLoggedIn,
                    onToggleEnabled = viewModel::setAniListEnabled,
                    onLogin = { viewModel.loginToAniList() },
                    onLogout = { viewModel.logoutFromAniList() },
                    onConfigure = { viewModel.configureAniList() }
                )
            }
            
            // Kitsu
            item {
                TrackingServiceItem(
                    serviceName = "Kitsu",
                    serviceIcon = Icons.Outlined.Pets, // TODO: Use actual Kitsu icon
                    enabled = kitsuEnabled,
                    loggedIn = kitsuLoggedIn,
                    onToggleEnabled = viewModel::setKitsuEnabled,
                    onLogin = { viewModel.loginToKitsu() },
                    onLogout = { viewModel.logoutFromKitsu() },
                    onConfigure = { viewModel.configureKitsu() }
                )
            }
            
            // MangaUpdates
            item {
                TrackingServiceItem(
                    serviceName = "MangaUpdates",
                    serviceIcon = Icons.Outlined.Update, // TODO: Use actual MU icon
                    enabled = mangaUpdatesEnabled,
                    loggedIn = mangaUpdatesLoggedIn,
                    onToggleEnabled = viewModel::setMangaUpdatesEnabled,
                    onLogin = { viewModel.loginToMangaUpdates() },
                    onLogout = { viewModel.logoutFromMangaUpdates() },
                    onConfigure = { viewModel.configureMangaUpdates() }
                )
            }
            
            // Auto-Sync Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.auto_sync_1),
                    icon = Icons.Outlined.AutoMode
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.enable_auto_sync),
                    description = "Automatically sync reading progress with tracking services",
                    icon = Icons.Outlined.AutoMode,
                    checked = autoSyncEnabled,
                    onCheckedChange = viewModel::setAutoSyncEnabled
                )
            }
            
            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.sync_interval),
                    description = "How often to sync with tracking services",
                    icon = Icons.Outlined.Schedule,
                    onClick = { viewModel.showSyncIntervalDialog() },
                    enabled = autoSyncEnabled
                ) {
                    Text(
                        text = when (autoSyncInterval) {
                            15 -> "15 minutes"
                            30 -> "30 minutes"
                            60 -> "1 hour"
                            180 -> "3 hours"
                            360 -> "6 hours"
                            720 -> "12 hours"
                            1440 -> "Daily"
                            else -> "1 hour"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (autoSyncEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.sync_only_over_wifi),
                    description = "Restrict syncing to WiFi connections only",
                    icon = Icons.Outlined.Wifi,
                    checked = syncOnlyOverWifi,
                    onCheckedChange = viewModel::setSyncOnlyOverWifi,
                    enabled = autoSyncEnabled
                )
            }
            
            // Auto-Update Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.auto_update_2),
                    icon = Icons.Outlined.Update
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.auto_update_status),
                    description = "Automatically update reading status (reading, completed, etc.)",
                    icon = Icons.Outlined.PlaylistAddCheck,
                    checked = autoUpdateStatus,
                    onCheckedChange = viewModel::setAutoUpdateStatus,
                    enabled = autoSyncEnabled
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.auto_update_progress),
                    description = "Automatically update chapter progress",
                    icon = Icons.Outlined.TrendingUp,
                    checked = autoUpdateProgress,
                    onCheckedChange = viewModel::setAutoUpdateProgress,
                    enabled = autoSyncEnabled
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.auto_update_score),
                    description = "Automatically sync ratings and scores",
                    icon = Icons.Outlined.Star,
                    checked = autoUpdateScore,
                    onCheckedChange = viewModel::setAutoUpdateScore,
                    enabled = autoSyncEnabled
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
                    title = localizeHelper.localize(Res.string.sync_history),
                    description = "View sync history and resolve conflicts",
                    icon = Icons.Outlined.History,
                    onClick = { viewModel.navigateToSyncHistory() }
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.manual_sync),
                    description = "Force sync all tracked books now",
                    icon = Icons.Outlined.Sync,
                    onClick = { viewModel.performManualSync() }
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.clear_sync_data),
                    description = "Remove all tracking data and start fresh",
                    icon = Icons.Outlined.ClearAll,
                    onClick = { viewModel.showClearSyncDataDialog() }
                )
            }
        }
    }
    
    // Sync Interval Dialog
    if (viewModel.showSyncIntervalDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSyncIntervalDialog() },
            title = { Text(localizeHelper.localize(Res.string.sync_interval)) },
            text = {
                Column {
                    val intervals = listOf(
                        15 to "15 minutes",
                        30 to "30 minutes",
                        60 to "1 hour",
                        180 to "3 hours",
                        360 to "6 hours",
                        720 to "12 hours",
                        1440 to "Daily"
                    )
                    intervals.forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = autoSyncInterval == value,
                                onClick = { 
                                    viewModel.setAutoSyncInterval(value)
                                    viewModel.dismissSyncIntervalDialog()
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
                TextButton(onClick = { viewModel.dismissSyncIntervalDialog() }) {
                    Text(localizeHelper.localize(Res.string.ok))
                }
            }
        )
    }
    
    // Clear Sync Data Confirmation Dialog
    if (viewModel.showClearSyncDataDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClearSyncDataDialog() },
            title = { Text(localizeHelper.localize(Res.string.clear_sync_data)) },
            text = {
                Text(localizeHelper.localize(Res.string.clear_sync_data_confirmation))
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.clearAllSyncData()
                        viewModel.dismissClearSyncDataDialog()
                    }
                ) {
                    Text(localizeHelper.localize(Res.string.clear_1))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissClearSyncDataDialog() }) {
                    Text(localizeHelper.localize(Res.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun TrackingServiceItem(
    serviceName: String,
    serviceIcon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    loggedIn: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onConfigure: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = serviceIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = serviceName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = when {
                            !enabled -> "Disabled"
                            loggedIn -> "Logged in"
                            else -> "Not logged in"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant
                            loggedIn -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
                
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
            
            if (enabled) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (loggedIn) {
                        OutlinedButton(
                            onClick = onLogout,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(localizeHelper.localize(Res.string.logout))
                        }
                        
                        Button(
                            onClick = onConfigure,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(localizeHelper.localize(Res.string.configure))
                        }
                    } else {
                        Button(
                            onClick = onLogin,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(localizeHelper.localize(Res.string.login))
                        }
                    }
                }
            }
        }
    }
}
