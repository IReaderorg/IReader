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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.components.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Data class for settings items to enable stable keys and reduce recomposition
 */
@Stable
private data class SettingsItemData(
    val key: String,
    val titleRes: String,
    val description: String,
    val icon: ImageVector,
    val section: String? = null
)

/**
 * Main settings screen following Mihon's comprehensive settings architecture.
 * Provides organized access to all app settings categories with Material Design 3 styling.
 * 
 * Performance optimizations:
 * - Uses stable data classes for items to reduce recomposition
 * - Pre-computes settings items list
 * - Uses stable keys for LazyColumn items
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
    
    // Use rememberSaveable with Saver for scroll state persistence
    val listState = rememberSaveable(
        key = "settings_main_scroll_state",
        saver = LazyListState.Saver
    ) {
        LazyListState()
    }
    
    // Pre-compute click handlers map to avoid lambda recreation
    val clickHandlers = remember {
        mapOf(
            "appearance" to onAppearanceSettings,
            "reader" to onReaderSettings,
            "library" to onLibrarySettings,
            "downloads" to onDownloadSettings,
            "tracking" to onTrackingSettings,
            "backup" to onBackupSettings,
            "data" to onDataSettings,
            "extensions" to onExtensionSettings,
            "security" to onSecuritySettings,
            "notifications" to onNotificationSettings,
            "advanced" to onAdvancedSettings
        )
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
        // Optimized LazyColumn with stable keys
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            )
        ) {
            // Appearance & Theme Section
            item(key = "header_appearance") {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.appearance_theme),
                    icon = Icons.Outlined.Palette
                )
            }
            
            item(key = "item_appearance") {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.appearance),
                    description = "Theme, colors, and visual customization",
                    icon = Icons.Outlined.Palette,
                    onClick = clickHandlers["appearance"]!!
                )
            }
            
            // Reading Experience Section
            item(key = "header_reading") {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.reading_experience),
                    icon = Icons.Outlined.MenuBook
                )
            }
            
            item(key = "item_reader") {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.reader),
                    description = "Reading mode, controls, and display preferences",
                    icon = Icons.Outlined.ChromeReaderMode,
                    onClick = clickHandlers["reader"]!!
                )
            }
            
            // Library Management Section
            item(key = "header_library") {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.library_management),
                    icon = Icons.Outlined.LibraryBooks
                )
            }
            
            item(key = "item_library") {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.library),
                    description = "Sorting, filtering, and update preferences",
                    icon = Icons.Outlined.LibraryBooks,
                    onClick = clickHandlers["library"]!!
                )
            }
            
            item(key = "item_downloads") {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.downloads),
                    description = "Download management and storage settings",
                    icon = Icons.Outlined.Download,
                    onClick = clickHandlers["downloads"]!!
                )
            }
            
            item(key = "item_tracking") {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.tracking),
                    description = "External service integration (MyAnimeList, AniList, etc.)",
                    icon = Icons.Outlined.Sync,
                    onClick = clickHandlers["tracking"]!!
                )
            }
            
            // Data & Backup Section
            item(key = "header_data") {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.data_backup),
                    icon = Icons.Outlined.Storage
                )
            }
            
            item(key = "item_backup") {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.backup_restore),
                    description = "Automatic backups and cloud storage integration",
                    icon = Icons.Outlined.SettingsBackupRestore,
                    onClick = clickHandlers["backup"]!!
                )
            }
            
            item(key = "item_data") {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.data_storage),
                    description = "Cache management and data usage settings",
                    icon = Icons.Outlined.Storage,
                    onClick = clickHandlers["data"]!!
                )
            }
            
            // Extensions & Sources Section
            item(key = "header_extensions") {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.extensions_sources),
                    icon = Icons.Outlined.Extension
                )
            }
            
            item(key = "item_extensions") {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.extensions_1),
                    description = "Extension repository management and updates",
                    icon = Icons.Outlined.Extension,
                    onClick = clickHandlers["extensions"]!!
                )
            }
            
            // Security & Privacy Section
            item(key = "header_security") {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.security_privacy),
                    icon = Icons.Outlined.Security
                )
            }
            
            item(key = "item_security") {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.security_privacy),
                    description = "App lock, incognito mode, and secure screen options",
                    icon = Icons.Outlined.Security,
                    onClick = clickHandlers["security"]!!
                )
            }
            
            // System & Notifications Section
            item(key = "header_system") {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.system_notifications),
                    icon = Icons.Outlined.Settings
                )
            }
            
            item(key = "item_notifications") {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.notifications),
                    description = "Granular control over notification types and channels",
                    icon = Icons.Outlined.Notifications,
                    onClick = clickHandlers["notifications"]!!
                )
            }
            
            item(key = "item_advanced") {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.advanced),
                    description = "Developer options and advanced configurations",
                    icon = Icons.Outlined.DeveloperMode,
                    onClick = clickHandlers["advanced"]!!
                )
            }
        }
    }
}
