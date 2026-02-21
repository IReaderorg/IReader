package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ireader.domain.services.platform.PlatformCapabilities
import ireader.domain.services.platform.PlatformType
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.components.SettingsItem
import ireader.presentation.ui.settings.components.SettingsSectionHeader
import ireader.presentation.core.safePopBackStack
import org.koin.compose.koinInject
/**
 * Data class representing a searchable setting item
 */
private data class SearchableSettingItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val section: String,
    val onClick: () -> Unit
)

class SettingScreenSpec {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        val platformCapabilities: PlatformCapabilities = koinInject()
        val isDesktop = platformCapabilities.platformType == PlatformType.DESKTOP
        
        // Search state
        var searchQuery by rememberSaveable { mutableStateOf("") }
        var isSearchActive by rememberSaveable { mutableStateOf(false) }
        val focusManager = LocalFocusManager.current
        val focusRequester = remember { FocusRequester() }
        
        // Build searchable settings list (filtered by platform)
        val searchableSettings = remember(isDesktop) {
            buildList {
                // Appearance & Display
                add(SearchableSettingItem(
                    id = "appearance",
                    title = localizeHelper.localize(Res.string.appearance),
                    description = "Customize app theme and colors",
                    icon = Icons.Default.Palette,
                    section = localizeHelper.localize(Res.string.appearance),
                    onClick = { navController.navigate(NavigationRoutes.appearance) }
                ))
                add(SearchableSettingItem(
                    id = "font",
                    title = localizeHelper.localize(Res.string.font),
                    description = "Choose reading fonts and sizes",
                    icon = Icons.Default.FontDownload,
                    section = localizeHelper.localize(Res.string.appearance),
                    onClick = { navController.navigate(NavigationRoutes.fontSettings) }
                ))
                add(SearchableSettingItem(
                    id = "theme",
                    title = localizeHelper.localize(Res.string.theme),
                    description = "Light, dark, or system default theme",
                    icon = Icons.Default.DarkMode,
                    section = localizeHelper.localize(Res.string.appearance),
                    onClick = { navController.navigate(NavigationRoutes.appearance) }
                ))
                add(SearchableSettingItem(
                    id = "colors",
                    title = localizeHelper.localize(Res.string.colors),
                    description = "Dynamic colors and Material You",
                    icon = Icons.Default.ColorLens,
                    section = localizeHelper.localize(Res.string.appearance),
                    onClick = { navController.navigate(NavigationRoutes.appearance) }
                ))
                
                // General Settings
                add(SearchableSettingItem(
                    id = "general",
                    title = localizeHelper.localize(Res.string.general),
                    description = "General app preferences",
                    icon = Icons.Default.Tune,
                    section = localizeHelper.localize(Res.string.general),
                    onClick = { navController.navigate(NavigationRoutes.generalSettings) }
                ))
                add(SearchableSettingItem(
                    id = "translation",
                    title = localizeHelper.localize(Res.string.translation_settings),
                    description = "Configure translation preferences",
                    icon = Icons.Default.Translate,
                    section = localizeHelper.localize(Res.string.general),
                    onClick = { navController.navigate(NavigationRoutes.translationSettings) }
                ))
                add(SearchableSettingItem(
                    id = "supabase",
                    title = localizeHelper.localize(Res.string.supabase_configuration),
                    description = "Configure custom Supabase instance for sync",
                    icon = Icons.Outlined.Cloud,
                    section = localizeHelper.localize(Res.string.general),
                    onClick = { navController.navigate(NavigationRoutes.supabaseConfig) }
                ))
                add(SearchableSettingItem(
                    id = "language",
                    title = localizeHelper.localize(Res.string.language),
                    description = "App language settings",
                    icon = Icons.Default.Language,
                    section = localizeHelper.localize(Res.string.general),
                    onClick = { navController.navigate(NavigationRoutes.generalSettings) }
                ))
                
                // Reading Experience
                add(SearchableSettingItem(
                    id = "reader",
                    title = localizeHelper.localize(Res.string.reader),
                    description = "Customize reading experience",
                    icon = Icons.Default.ChromeReaderMode,
                    section = localizeHelper.localize(Res.string.reader),
                    onClick = { navController.navigate(NavigationRoutes.readerSettings) }
                ))
                add(SearchableSettingItem(
                    id = "tts",
                    title = localizeHelper.localize(Res.string.tts_engine_manager),
                    description = "Configure text-to-speech engines and voices",
                    icon = Icons.Default.RecordVoiceOver,
                    section = localizeHelper.localize(Res.string.reader),
                    onClick = { navController.navigate(NavigationRoutes.ttsEngineManager) }
                ))
                add(SearchableSettingItem(
                    id = "statistics",
                    title = localizeHelper.localize(Res.string.statistics),
                    description = "View reading statistics and progress",
                    icon = Icons.Default.BarChart,
                    section = localizeHelper.localize(Res.string.reader),
                    onClick = { navController.navigate(NavigationRoutes.readingHub) }
                ))
                add(SearchableSettingItem(
                    id = "font_size",
                    title = localizeHelper.localize(Res.string.font_size),
                    description = "Adjust text size for reading",
                    icon = Icons.Default.FormatSize,
                    section = localizeHelper.localize(Res.string.reader),
                    onClick = { navController.navigate(NavigationRoutes.readerSettings) }
                ))
                add(SearchableSettingItem(
                    id = "reading_mode",
                    title = localizeHelper.localize(Res.string.reading_mode),
                    description = "Scroll or page reading mode",
                    icon = Icons.Default.MenuBook,
                    section = localizeHelper.localize(Res.string.reader),
                    onClick = { navController.navigate(NavigationRoutes.readerSettings) }
                ))
                
                // Security & Privacy
                add(SearchableSettingItem(
                    id = "security",
                    title = localizeHelper.localize(Res.string.security),
                    description = "Manage security and privacy settings",
                    icon = Icons.Default.Security,
                    section = localizeHelper.localize(Res.string.security),
                    onClick = { navController.navigate(NavigationRoutes.securitySettings) }
                ))
                add(SearchableSettingItem(
                    id = "incognito",
                    title = localizeHelper.localize(Res.string.pref_incognito_mode),
                    description = localizeHelper.localize(Res.string.pref_incognito_mode_summary),
                    icon = Icons.Default.VisibilityOff,
                    section = localizeHelper.localize(Res.string.security),
                    onClick = { navController.navigate(NavigationRoutes.securitySettings) }
                ))
                
                // Advanced
                add(SearchableSettingItem(
                    id = "repository",
                    title = localizeHelper.localize(Res.string.repository),
                    description = "Manage content sources and extensions",
                    icon = Icons.Default.Extension,
                    section = localizeHelper.localize(Res.string.advanced),
                    onClick = { navController.navigate(NavigationRoutes.repository) }
                ))
                add(SearchableSettingItem(
                    id = "plugins",
                    title = "Installed Plugins",
                    description = "Manage installed plugins and features",
                    icon = Icons.Default.Apps,
                    section = localizeHelper.localize(Res.string.advanced),
                    onClick = { navController.navigate(NavigationRoutes.pluginManagement) }
                ))
                add(SearchableSettingItem(
                    id = "feature_store",
                    title = "Feature Store",
                    description = "Browse and install plugins",
                    icon = Icons.Default.ShoppingCart,
                    section = localizeHelper.localize(Res.string.advanced),
                    onClick = { navController.navigate(NavigationRoutes.featureStore) }
                ))
                // Cloudflare Bypass - Desktop only (FlareSolverr doesn't work on Android/iOS)
                if (isDesktop) {
                    add(SearchableSettingItem(
                        id = "cloudflare_bypass",
                        title = "Cloudflare Bypass",
                        description = "Configure FlareSolverr for protected sources",
                        icon = Icons.Default.Shield,
                        section = localizeHelper.localize(Res.string.advanced),
                        onClick = { navController.navigate(NavigationRoutes.cloudflareBypass) }
                    ))
                }
                add(SearchableSettingItem(
                    id = "advanced",
                    title = localizeHelper.localize(Res.string.advance_setting),
                    description = "Advanced configuration options",
                    icon = Icons.Default.Code,
                    section = localizeHelper.localize(Res.string.advanced),
                    onClick = { navController.navigate(NavigationRoutes.advanceSettings) }
                ))
                add(SearchableSettingItem(
                    id = "cache",
                    title = localizeHelper.localize(Res.string.clear_all_cache),
                    description = "Clear cached data to free up space",
                    icon = Icons.Default.DeleteSweep,
                    section = localizeHelper.localize(Res.string.advanced),
                    onClick = { navController.navigate(NavigationRoutes.advanceSettings) }
                ))
                
                // Tracking
                add(SearchableSettingItem(
                    id = "tracking",
                    title = localizeHelper.localize(Res.string.tracking),
                    description = "Sync reading progress with AniList, MAL, and more",
                    icon = Icons.Default.Sync,
                    section = localizeHelper.localize(Res.string.tracking),
                    onClick = { navController.navigate(NavigationRoutes.trackingSettings) }
                ))
                
                // WiFi Sync
                add(SearchableSettingItem(
                    id = "wifi_sync",
                    title = "WiFi Sync",
                    description = "Sync books and progress between devices on local network",
                    icon = Icons.Default.Wifi,
                    section = "Sync",
                    onClick = { navController.navigate(NavigationRoutes.wifiSync) }
                ))
            }
        }
        
        // Filter settings based on search query
        val filteredSettings by remember(searchQuery, searchableSettings) {
            derivedStateOf {
                if (searchQuery.isBlank()) {
                    searchableSettings
                } else {
                    searchableSettings.filter { item ->
                        item.title.contains(searchQuery, ignoreCase = true) ||
                        item.description.contains(searchQuery, ignoreCase = true) ||
                        item.section.contains(searchQuery, ignoreCase = true)
                    }
                }
            }
        }
        
        // Request focus when search becomes active
        LaunchedEffect(isSearchActive) {
            if (isSearchActive) {
                focusRequester.requestFocus()
            }
        }
        
        IScaffold(
            topBar = { scrollBehavior ->
                Toolbar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        if (isSearchActive) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                placeholder = {
                                    Text(
                                        text = localize(Res.string.search_settings),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = localize(Res.string.clear_search)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { }),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        } else {
                            BigSizeTextComposable(text = localize(Res.string.settings))
                        }
                    },
                    navigationIcon = {
                        TopAppBarBackButton(onClick = {
                            if (isSearchActive) {
                                searchQuery = ""
                                isSearchActive = false
                                focusManager.clearFocus()
                            } else {
                                navController.safePopBackStack()
                            }
                        })
                    },
                    actions = {
                        if (!isSearchActive) {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = localize(Res.string.search)
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->
            if (isSearchActive) {
                // Group results by section - cached for performance (must be outside LazyColumn)
                val groupedResults = remember(filteredSettings) { filteredSettings.groupBy { it.section } }
                
                // Search Results
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    if (filteredSettings.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.SearchOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = localize(Res.string.no_settings_found),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = localize(Res.string.try_different_search),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    } else {
                        // Group results by section
                        groupedResults.forEach { (section, items) ->
                            item(key = "section_$section") {
                                Text(
                                    text = section,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            items(items, key = { it.id }) { item ->
                                SettingsItem(
                                    title = item.title,
                                    description = item.description,
                                    icon = item.icon,
                                    onClick = {
                                        item.onClick()
                                        searchQuery = ""
                                        isSearchActive = false
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Normal Settings List
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Appearance & Display Section
                    item {
                        SettingsSectionHeader(
                            title = localizeHelper.localize(Res.string.appearance),
                            icon = Icons.Default.Palette
                        )
                    }
                    
                    item {
                        SettingsItem(
                            title = localizeHelper.localize(Res.string.appearance),
                            description = "Customize app theme and colors",
                            icon = Icons.Default.Palette,
                            onClick = { navController.navigate(NavigationRoutes.appearance) }
                        )
                    }
                    
                    item {
                        SettingsItem(
                            title = localizeHelper.localize(Res.string.font),
                            description = "Choose reading fonts and sizes",
                            icon = Icons.Default.FontDownload,
                            onClick = { navController.navigate(NavigationRoutes.fontSettings) }
                        )
                    }
                    
                    // General Settings Section
                    item {
                        SettingsSectionHeader(
                            title = localizeHelper.localize(Res.string.general),
                            icon = Icons.Default.Tune
                        )
                    }
                    
                    item {
                        SettingsItem(
                            title = localizeHelper.localize(Res.string.general),
                            description = "General app preferences",
                            icon = Icons.Default.Tune,
                            onClick = { navController.navigate(NavigationRoutes.generalSettings) }
                        )
                    }
                    
                    item {
                        SettingsItem(
                            title = localizeHelper.localize(Res.string.translation_settings),
                            description = "Configure translation preferences",
                            icon = Icons.Default.Translate,
                            onClick = { navController.navigate(NavigationRoutes.translationSettings) }
                        )
                    }
                    item {
                        SettingsItem(
                            title = localizeHelper.localize(Res.string.supabase_configuration),
                            description = "Configure custom Supabase instance for sync",
                            icon = Icons.Outlined.Cloud,
                            onClick = { navController.navigate(NavigationRoutes.supabaseConfig) }
                        )
                    }

                    // Reading Experience Section
                    item {
                        SettingsSectionHeader(
                            title = localizeHelper.localize(Res.string.reader),
                            icon = Icons.Default.ChromeReaderMode
                        )
                    }
                    
                    item {
                        SettingsItem(
                            title = localizeHelper.localize(Res.string.reader),
                            description = "Customize reading experience",
                            icon = Icons.Default.ChromeReaderMode,
                            onClick = { navController.navigate(NavigationRoutes.readerSettings) }
                        )
                    }
                    
                    item {
                        SettingsItem(
                            title = localizeHelper.localize(Res.string.tts_engine_manager),
                            description = "Configure text-to-speech engines and voices",
                            icon = Icons.Default.RecordVoiceOver,
                            onClick = { navController.navigate(NavigationRoutes.ttsEngineManager) }
                        )
                    }
                    
                    item {
                        SettingsItem(
                            title = localizeHelper.localize(Res.string.statistics),
                            description = "View reading statistics and progress",
                            icon = Icons.Default.BarChart,
                            onClick = { navController.navigate(NavigationRoutes.readingHub) }
                        )
                    }
                    
                    // Security & Privacy Section
                    item {
                        SettingsSectionHeader(
                            title = localizeHelper.localize(Res.string.security),
                            icon = Icons.Default.Security
                        )
                    }
                    
                    item {
                        SettingsItem(
                            title = localizeHelper.localize(Res.string.security),
                            description = "Manage security and privacy settings",
                            icon = Icons.Default.Security,
                            onClick = { navController.navigate(NavigationRoutes.securitySettings) }
                        )
                    }
                    
                    // Tracking Section
                    item {
                        SettingsSectionHeader(
                            title = localizeHelper.localize(Res.string.tracking),
                            icon = Icons.Default.Sync
                        )
                    }
                    
                    item {
                        SettingsItem(
                            title = localizeHelper.localize(Res.string.tracking),
                            description = "Sync reading progress with AniList, MAL, and more",
                            icon = Icons.Default.Sync,
                            onClick = { navController.navigate(NavigationRoutes.trackingSettings) }
                        )
                    }
                    
                    item {
                        SettingsItem(
                            title = "WiFi Sync",
                            description = "Sync books and progress between devices on local network",
                            icon = Icons.Default.Wifi,
                            onClick = { navController.navigate(NavigationRoutes.wifiSync) }
                        )
                    }
                    
                    // Advanced Section
                    item {
                        SettingsSectionHeader(
                            title = localizeHelper.localize(Res.string.advanced),
                            icon = Icons.Default.Code
                        )
                    }
                    
                    item {
                        SettingsItem(
                            title = localizeHelper.localize(Res.string.repository),
                            description = "Manage content sources and extensions",
                            icon = Icons.Default.Extension,
                            onClick = { navController.navigate(NavigationRoutes.repository) }
                        )
                    }
                    
                    item {
                        SettingsItem(
                            title = "Installed Plugins",
                            description = "Manage installed plugins and features",
                            icon = Icons.Default.Apps,
                            onClick = { navController.navigate(NavigationRoutes.pluginManagement) }
                        )
                    }
                    
                    item {
                        SettingsItem(
                            title = "Feature Store",
                            description = "Browse and install plugins",
                            icon = Icons.Default.ShoppingCart,
                            onClick = { navController.navigate(NavigationRoutes.featureStore) }
                        )
                    }
                    
                    // Cloudflare Bypass - Desktop only (FlareSolverr doesn't work on Android/iOS)
                    if (isDesktop) {
                        item {
                            SettingsItem(
                                title = "Cloudflare Bypass",
                                description = "Configure FlareSolverr for protected sources",
                                icon = Icons.Default.Shield,
                                onClick = { navController.navigate(NavigationRoutes.cloudflareBypass) }
                            )
                        }
                    }
                    
                    item {
                        SettingsItem(
                            title = localizeHelper.localize(Res.string.advance_setting),
                            description = "Advanced configuration options",
                            icon = Icons.Default.Code,
                            onClick = { navController.navigate(NavigationRoutes.advanceSettings) }
                        )
                    }
                }
            }
        }
    }
}
