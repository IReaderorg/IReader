package ireader.presentation.ui.settings.notifications

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
 * Enhanced notification settings screen following Mihon's comprehensive notification system.
 * Provides granular control over notification types, channels, and behavior settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsNotificationScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    viewModel: SettingsNotificationViewModel,
    scaffoldPaddingValues: PaddingValues = PaddingValues()
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val listState = rememberSaveable(
        key = "settings_notification_scroll_state",
        saver = LazyListState.Saver
    ) {
        LazyListState()
    }

    // Notification preferences state
    val libraryUpdateNotifications by viewModel.libraryUpdateNotifications.collectAsState()
    val newChapterNotifications by viewModel.newChapterNotifications.collectAsState()
    val downloadProgressNotifications by viewModel.downloadProgressNotifications.collectAsState()
    val downloadCompleteNotifications by viewModel.downloadCompleteNotifications.collectAsState()
    val backupNotifications by viewModel.backupNotifications.collectAsState()
    val appUpdateNotifications by viewModel.appUpdateNotifications.collectAsState()
    val extensionUpdateNotifications by viewModel.extensionUpdateNotifications.collectAsState()
    val errorNotifications by viewModel.errorNotifications.collectAsState()
    
    val notificationSound by viewModel.notificationSound.collectAsState()
    val notificationVibration by viewModel.notificationVibration.collectAsState()
    val notificationLED by viewModel.notificationLED.collectAsState()
    val quietHoursEnabled by viewModel.quietHoursEnabled.collectAsState()
    val quietHoursStart by viewModel.quietHoursStart.collectAsState()
    val quietHoursEnd by viewModel.quietHoursEnd.collectAsState()
    val groupNotifications by viewModel.groupNotifications.collectAsState()

    IScaffold(
        modifier = modifier,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = localizeHelper.localize(Res.string.notifications),
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
            // Library Notifications Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.library_notifications),
                    icon = Icons.Outlined.LibraryBooks
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.library_update_progress),
                    description = "Show progress when updating library",
                    icon = Icons.Outlined.Update,
                    checked = libraryUpdateNotifications,
                    onCheckedChange = viewModel::setLibraryUpdateNotifications
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.new_chapter_notifications),
                    description = "Notify when new chapters are found",
                    icon = Icons.Outlined.NewReleases,
                    checked = newChapterNotifications,
                    onCheckedChange = viewModel::setNewChapterNotifications
                )
            }
            
            // Download Notifications Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.download_notifications),
                    icon = Icons.Outlined.Download
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.download_progress),
                    description = "Show progress for active downloads",
                    icon = Icons.Outlined.Download,
                    checked = downloadProgressNotifications,
                    onCheckedChange = viewModel::setDownloadProgressNotifications
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.download_complete),
                    description = "Notify when downloads finish",
                    icon = Icons.Outlined.DownloadDone,
                    checked = downloadCompleteNotifications,
                    onCheckedChange = viewModel::setDownloadCompleteNotifications
                )
            }
            
            // System Notifications Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.system_notifications_1),
                    icon = Icons.Outlined.Settings
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.backup_restore),
                    description = "Notify about backup and restore operations",
                    icon = Icons.Outlined.SettingsBackupRestore,
                    checked = backupNotifications,
                    onCheckedChange = viewModel::setBackupNotifications
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.app_updates),
                    description = "Notify when app updates are available",
                    icon = Icons.Outlined.SystemUpdate,
                    checked = appUpdateNotifications,
                    onCheckedChange = viewModel::setAppUpdateNotifications
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.extension_updates),
                    description = "Notify when extension updates are available",
                    icon = Icons.Outlined.Extension,
                    checked = extensionUpdateNotifications,
                    onCheckedChange = viewModel::setExtensionUpdateNotifications
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.error_notifications),
                    description = "Show notifications for errors and failures",
                    icon = Icons.Outlined.Error,
                    checked = errorNotifications,
                    onCheckedChange = viewModel::setErrorNotifications
                )
            }
            
            // Notification Behavior Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.notification_behavior),
                    icon = Icons.Outlined.NotificationsActive
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.sound),
                    description = "Play sound for notifications",
                    icon = Icons.Outlined.VolumeUp,
                    checked = notificationSound,
                    onCheckedChange = viewModel::setNotificationSound
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.vibration),
                    description = "Vibrate for notifications",
                    icon = Icons.Outlined.Vibration,
                    checked = notificationVibration,
                    onCheckedChange = viewModel::setNotificationVibration
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.led_light),
                    description = "Flash LED for notifications (if available)",
                    icon = Icons.Outlined.Lightbulb,
                    checked = notificationLED,
                    onCheckedChange = viewModel::setNotificationLED
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.group_notifications),
                    description = "Group similar notifications together",
                    icon = Icons.Outlined.Group,
                    checked = groupNotifications,
                    onCheckedChange = viewModel::setGroupNotifications
                )
            }
            
            // Quiet Hours Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.quiet_hours),
                    icon = Icons.Outlined.DoNotDisturb
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = localizeHelper.localize(Res.string.enable_quiet_hours),
                    description = "Suppress notifications during specified hours",
                    icon = Icons.Outlined.DoNotDisturb,
                    checked = quietHoursEnabled,
                    onCheckedChange = viewModel::setQuietHoursEnabled
                )
            }
            
            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.start_time),
                    description = "When quiet hours begin",
                    icon = Icons.Outlined.Schedule,
                    onClick = { viewModel.showQuietHoursStartDialog() },
                    enabled = quietHoursEnabled
                ) {
                    Text(
                        text = formatTime(quietHoursStart),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (quietHoursEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }
            
            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.end_time),
                    description = "When quiet hours end",
                    icon = Icons.Outlined.Schedule,
                    onClick = { viewModel.showQuietHoursEndDialog() },
                    enabled = quietHoursEnabled
                ) {
                    Text(
                        text = formatTime(quietHoursEnd),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (quietHoursEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
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
                    title = localizeHelper.localize(Res.string.notification_channels),
                    description = "Manage system notification channels",
                    icon = Icons.Outlined.Tune,
                    onClick = { viewModel.openNotificationChannelSettings() }
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.test_notifications),
                    description = "Send test notifications to verify settings",
                    icon = Icons.Outlined.Send,
                    onClick = { viewModel.sendTestNotifications() }
                )
            }
        }
    }
    
    // Quiet Hours Start Time Dialog
    if (viewModel.showQuietHoursStartDialog) {
        TimePickerDialog(
            title = localizeHelper.localize(Res.string.quiet_hours_start),
            initialTime = quietHoursStart,
            onTimeSelected = { time ->
                viewModel.setQuietHoursStart(time)
                viewModel.dismissQuietHoursStartDialog()
            },
            onDismiss = { viewModel.dismissQuietHoursStartDialog() }
        )
    }
    
    // Quiet Hours End Time Dialog
    if (viewModel.showQuietHoursEndDialog) {
        TimePickerDialog(
            title = localizeHelper.localize(Res.string.quiet_hours_end),
            initialTime = quietHoursEnd,
            onTimeSelected = { time ->
                viewModel.setQuietHoursEnd(time)
                viewModel.dismissQuietHoursEndDialog()
            },
            onDismiss = { viewModel.dismissQuietHoursEndDialog() }
        )
    }
}

@Composable
private fun TimePickerDialog(
    title: String,
    initialTime: Pair<Int, Int>,
    onTimeSelected: (Pair<Int, Int>) -> Unit,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var selectedHour by remember { mutableStateOf(initialTime.first) }
    var selectedMinute by remember { mutableStateOf(initialTime.second) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = localizeHelper.localize(Res.string.select_time),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Hour picker
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text("Hour", style = MaterialTheme.typography.labelMedium)
                        OutlinedTextField(
                            value = selectedHour.toString().padStart(2, '0'),
                            onValueChange = { value ->
                                value.toIntOrNull()?.let { hour ->
                                    if (hour in 0..23) selectedHour = hour
                                }
                            },
                            modifier = Modifier.width(80.dp)
                        )
                    }
                    
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.CenterVertically)
                            .padding(horizontal = 8.dp)
                    )
                    
                    // Minute picker
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text("Minute", style = MaterialTheme.typography.labelMedium)
                        OutlinedTextField(
                            value = selectedMinute.toString().padStart(2, '0'),
                            onValueChange = { value ->
                                value.toIntOrNull()?.let { minute ->
                                    if (minute in 0..59) selectedMinute = minute
                                }
                            },
                            modifier = Modifier.width(80.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onTimeSelected(selectedHour to selectedMinute) }
            ) {
                Text(localizeHelper.localize(Res.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}

private fun formatTime(time: Pair<Int, Int>): String {
    val (hour, minute) = time
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}
