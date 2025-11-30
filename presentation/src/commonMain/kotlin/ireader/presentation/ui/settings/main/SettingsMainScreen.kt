package ireader.presentation.ui.settings.main

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
 * Main settings screen following Mihon's comprehensive settings architecture.
 * Provides organized access to all app settings categories with Material Design 3 styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMainScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onAppearanceSettings: () -> Unit,
    onReaderSettings: () -> Unit,
    onLibrarySettings: () -> Unit,
    onDownloadSettings: () -> Unit,
    onTrackingSettings: () -> Unit,
    onBackupSettings: () -> Unit,
    onSecuritySettings: () -> Unit,
    onAdvancedSettings: () -> Unit,
    onExtensionSettings: () -> Unit,
    onNotificationSettings: () -> Unit,
    onDataSettings: () -> Unit,
    scaffoldPaddingValues: PaddingValues = PaddingValues()
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val listState = rememberSaveable(
        key = "settings_main_scroll_state",
        saver = LazyListState.Saver
    ) {
        LazyListState()
    }

    IScaffold(
        modifier = modifier,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = localize(Res.string.settings),
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
            // Appearance & Theme Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.appearance_theme),
                    icon = Icons.Outlined.Palette
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.appearance),
                    description = "Theme, colors, and visual customization",
                    icon = Icons.Outlined.Palette,
                    onClick = onAppearanceSettings
                )
            }
            
            // Reading Experience Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.reading_experience),
                    icon = Icons.Outlined.MenuBook
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.reader),
                    description = "Reading mode, controls, and display preferences",
                    icon = Icons.Outlined.ChromeReaderMode,
                    onClick = onReaderSettings
                )
            }
            
            // Library Management Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.library_management),
                    icon = Icons.Outlined.LibraryBooks
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.library),
                    description = "Sorting, filtering, and update preferences",
                    icon = Icons.Outlined.LibraryBooks,
                    onClick = onLibrarySettings
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.downloads),
                    description = "Download management and storage settings",
                    icon = Icons.Outlined.Download,
                    onClick = onDownloadSettings
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.tracking),
                    description = "External service integration (MyAnimeList, AniList, etc.)",
                    icon = Icons.Outlined.Sync,
                    onClick = onTrackingSettings
                )
            }
            
            // Data & Backup Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.data_backup),
                    icon = Icons.Outlined.Storage
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.backup_restore),
                    description = "Automatic backups and cloud storage integration",
                    icon = Icons.Outlined.SettingsBackupRestore,
                    onClick = onBackupSettings
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.data_storage),
                    description = "Cache management and data usage settings",
                    icon = Icons.Outlined.Storage,
                    onClick = onDataSettings
                )
            }
            
            // Extensions & Sources Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.extensions_sources),
                    icon = Icons.Outlined.Extension
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.extensions_1),
                    description = "Extension repository management and updates",
                    icon = Icons.Outlined.Extension,
                    onClick = onExtensionSettings
                )
            }
            
            // Security & Privacy Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.security_privacy),
                    icon = Icons.Outlined.Security
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.security_privacy),
                    description = "App lock, incognito mode, and secure screen options",
                    icon = Icons.Outlined.Security,
                    onClick = onSecuritySettings
                )
            }
            
            // System & Notifications Section
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.system_notifications),
                    icon = Icons.Outlined.Settings
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.notifications),
                    description = "Granular control over notification types and channels",
                    icon = Icons.Outlined.Notifications,
                    onClick = onNotificationSettings
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.advanced),
                    description = "Developer options and advanced configurations",
                    icon = Icons.Outlined.DeveloperMode,
                    onClick = onAdvancedSettings
                )
            }
        }
    }
}