package ireader.presentation.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import ireader.domain.preferences.prefs.UiPreferences
import ireader.i18n.Images.incognito
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.Divider
import ireader.presentation.ui.component.components.LogoHeader
import ireader.presentation.ui.component.components.PreferenceRow
import ireader.presentation.ui.component.components.SwitchPreference
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.components.SettingsItem
import ireader.presentation.ui.settings.components.SettingsSectionHeader
import kotlinx.coroutines.launch
import androidx.compose.runtime.State

/**
 * Data class representing a searchable setting item
 */
data class SearchableSettingItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val section: String,
    val onClick: () -> Unit
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    modifier: Modifier = Modifier,
    vm: MainSettingScreenViewModel,
    onDownloadScreen: () -> Unit,
    onBackupScreen: () -> Unit,
    onCategory: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    onHelp: () -> Unit,
    onDonation: () -> Unit = {},
    onWeb3Profile: () -> Unit = {},
    onBadgeStore: () -> Unit = {},
    onNFTBadge: () -> Unit = {},
    onBadgeManagement: () -> Unit = {},
    onAdminBadgeVerification: () -> Unit = {},
    onLeaderboard: () -> Unit = {},
    onPopularBooks: () -> Unit = {},
    onAllReviews: () -> Unit = {},
    // Sub-settings screen navigation
    onAppearanceSettings: () -> Unit = {},
    onReaderSettings: () -> Unit = {},
    onGeneralSettings: () -> Unit = {},
    onSecuritySettings: () -> Unit = {},
    onAdvancedSettings: () -> Unit = {},
    onTranslationSettings: () -> Unit = {},
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }

    // Theme mode state
    var showThemeOptions by remember { mutableStateOf(false) }
    
    // Search state
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    
    // Build searchable settings list - includes items from More screen and sub-settings screens
    val searchableSettings = remember(vm.isAdmin.value) {
        buildList {
            // ===== MORE SCREEN ITEMS =====
            
            // Account section
            add(SearchableSettingItem(
                id = "profile_sync",
                title = localizeHelper.localize(Res.string.profile_sync),
                description = "Manage your account and sync reading progress",
                icon = Icons.Outlined.AccountCircle,
                section = localizeHelper.localize(Res.string.account),
                onClick = onWeb3Profile
            ))
            
            // Badges & Customization section
            add(SearchableSettingItem(
                id = "badge_store",
                title = localizeHelper.localize(Res.string.badge_store),
                description = "Purchase unique badges to customize your profile",
                icon = Icons.Outlined.AccountBalanceWallet,
                section = localizeHelper.localize(Res.string.badges_customization),
                onClick = onBadgeStore
            ))
            add(SearchableSettingItem(
                id = "nft_badge",
                title = localizeHelper.localize(Res.string.nft_badge),
                description = "Verify NFT ownership to unlock exclusive badge",
                icon = Icons.Filled.Star,
                section = localizeHelper.localize(Res.string.badges_customization),
                onClick = onNFTBadge
            ))
            add(SearchableSettingItem(
                id = "manage_badges",
                title = localizeHelper.localize(Res.string.manage_badges),
                description = "Customize which badges appear on your profile and reviews",
                icon = Icons.Outlined.Settings,
                section = localizeHelper.localize(Res.string.badges_customization),
                onClick = onBadgeManagement
            ))
            
            // Admin section (only for admins)
            if (vm.isAdmin.value) {
                add(SearchableSettingItem(
                    id = "badge_verification",
                    title = localizeHelper.localize(Res.string.badge_verification),
                    description = "Review and approve badge purchase requests",
                    icon = Icons.Outlined.VerifiedUser,
                    section = localizeHelper.localize(Res.string.admin),
                    onClick = onAdminBadgeVerification
                ))
            }
            
            // Community section
            add(SearchableSettingItem(
                id = "leaderboard",
                title = localizeHelper.localize(Res.string.leaderboard),
                description = "Compete with other readers based on reading time",
                icon = Icons.Filled.EmojiEvents,
                section = localizeHelper.localize(Res.string.community),
                onClick = onLeaderboard
            ))
            add(SearchableSettingItem(
                id = "popular_books",
                title = localizeHelper.localize(Res.string.popular_books),
                description = "Discover what the community is reading",
                icon = Icons.Filled.TrendingUp,
                section = localizeHelper.localize(Res.string.community),
                onClick = onPopularBooks
            ))
            add(SearchableSettingItem(
                id = "community_reviews",
                title = localizeHelper.localize(Res.string.community_reviews),
                description = "Read reviews from other readers",
                icon = Icons.Filled.RateReview,
                section = localizeHelper.localize(Res.string.community),
                onClick = onAllReviews
            ))
            
            // Library Management section
            add(SearchableSettingItem(
                id = "download",
                title = localizeHelper.localize(Res.string.download),
                description = "Manage downloaded content",
                icon = Icons.Outlined.Download,
                section = localizeHelper.localize(Res.string.library_management),
                onClick = onDownloadScreen
            ))
            add(SearchableSettingItem(
                id = "backup_restore",
                title = localizeHelper.localize(Res.string.backup_and_restore),
                description = "Backup and restore your library",
                icon = Icons.Outlined.SettingsBackupRestore,
                section = localizeHelper.localize(Res.string.library_management),
                onClick = onBackupScreen
            ))
            add(SearchableSettingItem(
                id = "category",
                title = localizeHelper.localize(Res.string.category),
                description = "Organize books with categories",
                icon = Icons.Outlined.Label,
                section = localizeHelper.localize(Res.string.library_management),
                onClick = onCategory
            ))
            
            // Information & Support section
            add(SearchableSettingItem(
                id = "about",
                title = localizeHelper.localize(Res.string.about),
                description = "App info and credits",
                icon = Icons.Outlined.Info,
                section = localizeHelper.localize(Res.string.information_support),
                onClick = onAbout
            ))
            add(SearchableSettingItem(
                id = "help",
                title = localizeHelper.localize(Res.string.help),
                description = "Get help with the app",
                icon = Icons.Outlined.Help,
                section = localizeHelper.localize(Res.string.information_support),
                onClick = onHelp
            ))
            add(SearchableSettingItem(
                id = "donation",
                title = localizeHelper.localize(Res.string.support_development),
                description = "Help keep IReader free and ad-free",
                icon = Icons.Outlined.Favorite,
                section = localizeHelper.localize(Res.string.information_support),
                onClick = onDonation
            ))
            
            // ===== SETTINGS SCREEN ITEMS =====
            // These navigate to the specific sub-settings screens
            
            // Appearance & Theme - navigates to Appearance screen
            add(SearchableSettingItem(
                id = "settings_appearance",
                title = localizeHelper.localize(Res.string.appearance),
                description = "Theme, colors, and visual customization",
                icon = Icons.Outlined.Palette,
                section = localizeHelper.localize(Res.string.appearance),
                onClick = onAppearanceSettings
            ))
            add(SearchableSettingItem(
                id = "settings_theme",
                title = "Theme",
                description = "Light, dark, or system default theme",
                icon = Icons.Outlined.DarkMode,
                section = localizeHelper.localize(Res.string.appearance),
                onClick = onAppearanceSettings
            ))
            add(SearchableSettingItem(
                id = "settings_dynamic_colors",
                title = "Dynamic Colors",
                description = "Material You colors from wallpaper",
                icon = Icons.Outlined.AutoAwesome,
                section = localizeHelper.localize(Res.string.appearance),
                onClick = onAppearanceSettings
            ))
            add(SearchableSettingItem(
                id = "settings_amoled",
                title = "AMOLED Mode",
                description = "Pure black backgrounds for power saving",
                icon = Icons.Outlined.Brightness2,
                section = localizeHelper.localize(Res.string.appearance),
                onClick = onAppearanceSettings
            ))
            
            // Reader Settings - navigates to Reader screen
            add(SearchableSettingItem(
                id = "settings_reader",
                title = localizeHelper.localize(Res.string.reader),
                description = "Reading mode, controls, and display preferences",
                icon = Icons.Outlined.ChromeReaderMode,
                section = localizeHelper.localize(Res.string.reader),
                onClick = onReaderSettings
            ))
            add(SearchableSettingItem(
                id = "settings_font",
                title = localizeHelper.localize(Res.string.font),
                description = "Font family and text appearance",
                icon = Icons.Outlined.FontDownload,
                section = localizeHelper.localize(Res.string.reader),
                onClick = onReaderSettings
            ))
            add(SearchableSettingItem(
                id = "settings_font_size",
                title = localizeHelper.localize(Res.string.font_size),
                description = "Adjust text size for reading",
                icon = Icons.Outlined.FormatSize,
                section = localizeHelper.localize(Res.string.reader),
                onClick = onReaderSettings
            ))
            add(SearchableSettingItem(
                id = "settings_reading_mode",
                title = localizeHelper.localize(Res.string.reading_mode),
                description = "Scroll or page reading mode",
                icon = Icons.Outlined.MenuBook,
                section = localizeHelper.localize(Res.string.reader),
                onClick = onReaderSettings
            ))
            add(SearchableSettingItem(
                id = "settings_brightness",
                title = localizeHelper.localize(Res.string.custom_brightness),
                description = "Custom brightness for reading",
                icon = Icons.Outlined.BrightnessHigh,
                section = localizeHelper.localize(Res.string.reader),
                onClick = onReaderSettings
            ))
            add(SearchableSettingItem(
                id = "settings_screen_on",
                title = localizeHelper.localize(Res.string.screen_always_on),
                description = "Keep screen on while reading",
                icon = Icons.Outlined.ScreenLockPortrait,
                section = localizeHelper.localize(Res.string.reader),
                onClick = onReaderSettings
            ))
            
            // General Settings - navigates to General screen
            add(SearchableSettingItem(
                id = "settings_general",
                title = localizeHelper.localize(Res.string.general),
                description = "App behavior and preferences",
                icon = Icons.Outlined.Settings,
                section = localizeHelper.localize(Res.string.general),
                onClick = onGeneralSettings
            ))
            
            // Library Settings - navigates to General settings
            add(SearchableSettingItem(
                id = "settings_library",
                title = localizeHelper.localize(Res.string.library),
                description = "Sorting, filtering, and update preferences",
                icon = Icons.Outlined.LibraryBooks,
                section = localizeHelper.localize(Res.string.general),
                onClick = onGeneralSettings
            ))
            
            // Download Settings - navigates to General settings
            add(SearchableSettingItem(
                id = "settings_downloads",
                title = localizeHelper.localize(Res.string.downloads),
                description = "Download management and storage settings",
                icon = Icons.Outlined.Download,
                section = localizeHelper.localize(Res.string.general),
                onClick = onGeneralSettings
            ))
            
            // Tracking - navigates to Settings main
            add(SearchableSettingItem(
                id = "settings_tracking",
                title = localizeHelper.localize(Res.string.tracking),
                description = "External service integration (MyAnimeList, AniList)",
                icon = Icons.Outlined.Sync,
                section = localizeHelper.localize(Res.string.settings),
                onClick = onSettings
            ))
            
            // Security & Privacy - navigates to Security settings
            add(SearchableSettingItem(
                id = "settings_security",
                title = localizeHelper.localize(Res.string.security),
                description = "App lock, incognito mode, and secure screen",
                icon = Icons.Outlined.Security,
                section = localizeHelper.localize(Res.string.security),
                onClick = onSecuritySettings
            ))
            add(SearchableSettingItem(
                id = "settings_incognito",
                title = localizeHelper.localize(Res.string.pref_incognito_mode),
                description = localizeHelper.localize(Res.string.pref_incognito_mode_summary),
                icon = Icons.Outlined.VisibilityOff,
                section = localizeHelper.localize(Res.string.security),
                onClick = onSecuritySettings
            ))
            
            // Notifications - navigates to General settings
            add(SearchableSettingItem(
                id = "settings_notifications",
                title = localizeHelper.localize(Res.string.notifications),
                description = "Control notification types and channels",
                icon = Icons.Outlined.Notifications,
                section = localizeHelper.localize(Res.string.general),
                onClick = onGeneralSettings
            ))
            
            // Advanced - navigates to Advanced settings
            add(SearchableSettingItem(
                id = "settings_advanced",
                title = localizeHelper.localize(Res.string.advance_setting),
                description = "Developer options and advanced configurations",
                icon = Icons.Outlined.DeveloperMode,
                section = localizeHelper.localize(Res.string.advance_setting),
                onClick = onAdvancedSettings
            ))
            
            // ===== GENERAL SETTINGS ITEMS =====
            
            // Language & Translation - navigates to General settings
            add(SearchableSettingItem(
                id = "general_language",
                title = localizeHelper.localize(Res.string.language),
                description = "App language and translation settings",
                icon = Icons.Outlined.Language,
                section = localizeHelper.localize(Res.string.general),
                onClick = onGeneralSettings
            ))
            add(SearchableSettingItem(
                id = "general_translation",
                title = localizeHelper.localize(Res.string.translation_settings),
                description = "Configure translation API and settings",
                icon = Icons.Outlined.Translate,
                section = localizeHelper.localize(Res.string.general),
                onClick = onTranslationSettings
            ))
            
            // App Updates - navigates to General settings
            add(SearchableSettingItem(
                id = "general_updates",
                title = localizeHelper.localize(Res.string.updater_is_enable),
                description = "Check for app updates automatically",
                icon = Icons.Outlined.Update,
                section = localizeHelper.localize(Res.string.general),
                onClick = onGeneralSettings
            ))
            
            // History - navigates to General settings
            add(SearchableSettingItem(
                id = "general_history",
                title = localizeHelper.localize(Res.string.show_history),
                description = "Enable reading history tracking",
                icon = Icons.Outlined.History,
                section = localizeHelper.localize(Res.string.general),
                onClick = onGeneralSettings
            ))
            
            // Data & Storage - navigates to Advanced settings
            add(SearchableSettingItem(
                id = "settings_data",
                title = localizeHelper.localize(Res.string.data),
                description = "Cache management and data usage settings",
                icon = Icons.Outlined.Storage,
                section = localizeHelper.localize(Res.string.advance_setting),
                onClick = onAdvancedSettings
            ))
            add(SearchableSettingItem(
                id = "settings_clear_cache",
                title = localizeHelper.localize(Res.string.clear_all_cache),
                description = "Clear cached data to free up space",
                icon = Icons.Outlined.DeleteSweep,
                section = localizeHelper.localize(Res.string.advance_setting),
                onClick = onAdvancedSettings
            ))
            
            // Extensions - navigates to General settings
            add(SearchableSettingItem(
                id = "settings_extensions",
                title = localizeHelper.localize(Res.string.extensions),
                description = "Extension repository management and updates",
                icon = Icons.Outlined.Extension,
                section = localizeHelper.localize(Res.string.general),
                onClick = onGeneralSettings
            ))
        }
    }
    
    // Filter settings based on search query
    val filteredSettings by remember(searchQuery, searchableSettings) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                searchableSettings // Show all settings when query is empty
            } else {
                searchableSettings.filter { item ->
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true) ||
                    item.section.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    // Create list state with saved position from ViewModel
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = vm.savedScrollIndex,
        initialFirstVisibleItemScrollOffset = vm.savedScrollOffset
    )

    // Save scroll position to ViewModel when it changes (with debounce to avoid too many saves)
    androidx.compose.runtime.LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        kotlinx.coroutines.delay(100) // Debounce to avoid saving on every pixel
        vm.saveScrollPosition(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset
        )
    }
    
    // Save position when leaving the screen
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            vm.saveScrollPosition(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset
            )
        }
    }
    
    // Request focus when search becomes active
    androidx.compose.runtime.LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }
    
    Column(modifier = modifier) {
        // Status bar spacer
        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars))
        
        // Search Bar Row - icon only when collapsed, full search when expanded
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search icon (shown when search is not active)
            androidx.compose.animation.AnimatedVisibility(
                visible = !isSearchActive,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                IconButton(
                    onClick = { isSearchActive = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = localize(Res.string.search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Search bar (shown when search is active)
            androidx.compose.animation.AnimatedVisibility(
                visible = isSearchActive,
                enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
                exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut(),
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingsSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClose = {
                        searchQuery = ""
                        isSearchActive = false
                        focusManager.clearFocus()
                    },
                    focusRequester = focusRequester,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Show search results or normal content
        if (isSearchActive) {
            // Search Results
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
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
                    val groupedResults = filteredSettings.groupBy { it.section }
                    groupedResults.forEach { (section, items) ->
                        item {
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
                                    // Clear search after navigation
                                    searchQuery = ""
                                    isSearchActive = false
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // Normal More Screen Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
        // App Logo and Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                LogoHeader()
            }
        }
        
        // Profile Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(16.dp)
            ) {
                // App Name
                Text(
                    text = localizeHelper.localize(Res.string.website),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // App Description
                Text(
                    text = localizeHelper.localize(Res.string.your_personal_book_reading_companion),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Incognito Mode Switch with enhanced UI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (vm.incognitoMode.value) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface)
                        .clickable { vm.incognitoMode.value = !vm.incognitoMode.value }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Incognito Icon in a circle
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (vm.incognitoMode.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = incognito(),
                            contentDescription = null,
                            tint = if (vm.incognitoMode.value) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = localize(Res.string.pref_incognito_mode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (vm.incognitoMode.value) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Text(
                            text = localize(Res.string.pref_incognito_mode_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (vm.incognitoMode.value) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = vm.incognitoMode.value,
                        onCheckedChange = { vm.incognitoMode.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedIconColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
        
        // Profile Section
        item {
            SettingsSectionHeader(
                title = localizeHelper.localize(Res.string.account),
                icon = Icons.Filled.AccountCircle
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(Res.string.profile_sync),
                description = "Manage your account and sync reading progress",
                icon = Icons.Outlined.AccountCircle,
                onClick = onWeb3Profile
            )
        }
        // Badges & Customization Section
        item {
            SettingsSectionHeader(
                title = localizeHelper.localize(Res.string.badges_customization),
                icon = Icons.Filled.Star
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(Res.string.badge_store),
                description = "Purchase unique badges to customize your profile",
                icon = Icons.Outlined.AccountBalanceWallet,
                onClick = onBadgeStore
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(Res.string.nft_badge),
                description = "Verify NFT ownership to unlock exclusive badge",
                icon = Icons.Filled.Star,
                onClick = onNFTBadge
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(Res.string.manage_badges),
                description = "Customize which badges appear on your profile and reviews",
                icon = Icons.Outlined.Settings,
                onClick = onBadgeManagement
            )
        }
        
        // Admin Section (only visible to admins)
        if (vm.isAdmin.value) {
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.admin),
                    icon = Icons.Filled.AdminPanelSettings
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.badge_verification),
                    description = "Review and approve badge purchase requests",
                    icon = Icons.Outlined.VerifiedUser,
                    onClick = onAdminBadgeVerification
                )
            }
        }
        
        // Community Section
        item {
            SettingsSectionHeader(
                title = localizeHelper.localize(Res.string.community),
                icon = Icons.Filled.People
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(Res.string.leaderboard),
                description = "Compete with other readers based on reading time",
                icon = Icons.Filled.EmojiEvents,
                onClick = onLeaderboard
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(Res.string.popular_books),
                description = "Discover what the community is reading",
                icon = Icons.Filled.TrendingUp,
                onClick = onPopularBooks
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(Res.string.community_reviews),
                description = "Read reviews from other readers",
                icon = Icons.Filled.RateReview,
                onClick = onAllReviews
            )
        }
        
        // Library Management Section
        item {
            SettingsSectionHeader(
                title = localizeHelper.localize(Res.string.library_management),
                icon = Icons.Filled.MenuBook
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(Res.string.download),
                description = "Manage downloaded content",
                icon = Icons.Outlined.Download,
                onClick = onDownloadScreen
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(Res.string.backup_and_restore),
                description = "Backup and restore your library",
                icon = Icons.Outlined.SettingsBackupRestore,
                onClick = onBackupScreen
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(Res.string.category),
                description = "Organize books with categories",
                icon = Icons.Outlined.Label,
                onClick = onCategory
            )
        }
        
        // Appearance & Settings Section
        item {
            SettingsSectionHeader(
                title = localizeHelper.localize(Res.string.appearance_settings),
                icon = Icons.Filled.Palette
            )
        }
        
//        // Theme selector item
//        item {
//            SettingsItemWithAction(
//                title = "Theme",
//                description = "Choose app theme and appearance",
//                icon = Icons.Outlined.DarkMode,
//                onClick = { showThemeOptions = !showThemeOptions },
//                endContent = {
//                    ThemeChip(
//                        text = "System default",
//                        onClick = { showThemeOptions = !showThemeOptions }
//                    )
//                },
//                isExpanded = showThemeOptions,
//                expandedContent = {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp)
//                    ) {
//                        ThemeOption(
//                            text = "Light",
//                            isSelected = false,
//                            onClick = { /* Handle light theme selection */ }
//                        )
//
//                        ThemeOption(
//                            text = "Dark",
//                            isSelected = true,
//                            onClick = { /* Handle dark theme selection */ }
//                        )
//
//                        ThemeOption(
//                            text = "System default",
//                            isSelected = false,
//                            onClick = { /* Handle system theme selection */ }
//                        )
//                    }
//                }
//            )
//        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(Res.string.settings),
                description = "Configure app preferences",
                icon = Icons.Outlined.Settings,
                onClick = onSettings
            )
        }
        

        
        // Information & Support Section
        item {
            SettingsSectionHeader(
                title = localizeHelper.localize(Res.string.information_support),
                icon = Icons.Filled.Info
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(Res.string.about),
                description = "App info and credits",
                icon = Icons.Outlined.Info,
                onClick = onAbout
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(Res.string.help),
                description = "Get help with the app",
                icon = Icons.Outlined.Help,
                onClick = onHelp
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(Res.string.support_development),
                description = "Help keep IReader free and ad-free",
                icon = Icons.Outlined.Favorite,
                onClick = onDonation
            )
        }
    }
        }
    }
}

/**
 * Search bar component for settings search
 */
@Composable
private fun SettingsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .focusRequester(focusRequester)
            .clip(RoundedCornerShape(28.dp)),
        placeholder = {
            Text(
                text = localize(Res.string.search_settings),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = localize(Res.string.search),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    if (query.isNotEmpty()) {
                        onQueryChange("")
                    } else {
                        onClose()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = localize(Res.string.clear_search),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                // Search is already happening via onQueryChange
            }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent
        ),
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun SettingsItemWithAction(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    endContent: @Composable () -> Unit,
    isExpanded: Boolean = false,
    expandedContent: @Composable () -> Unit = {}
) {
    Column {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                endContent()
            }
        }
        
        AnimatedVisibility(visible = isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 56.dp, 
                        end = 16.dp,
                        top = 0.dp,
                        bottom = 8.dp
                    )
            ) {
                expandedContent()
            }
        }
    }
}

@Composable
private fun ThemeChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ThemeOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

data class SettingsSection(
    val titleRes: StringResource,
    val icon: ImageVector? = null,
    val onClick: () -> Unit,
)

@Composable
fun SetupLayout(
    modifier: Modifier = Modifier,
    items: List<SettingsSection>,
    padding: PaddingValues? = null,
) {
    LazyColumn(
        modifier = if (padding != null) modifier.padding(padding) else modifier,
    ) {
        items.map {
            item {
                PreferenceRow(
                    title = localize(it.titleRes),
                    icon = it.icon,
                    onClick = it.onClick,
                )
            }
        }
    }
}



class MainSettingScreenViewModel(
    uiPreferences: UiPreferences,
    private val getCurrentUser: suspend () -> ireader.domain.models.remote.User?
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {
    val incognitoMode = uiPreferences.incognitoMode().asState()
    
    private val _isAdmin = androidx.compose.runtime.mutableStateOf(false)
    val isAdmin: androidx.compose.runtime.State<Boolean> = _isAdmin

    var savedScrollIndex by androidx.compose.runtime.mutableStateOf(0)
        private set
    
    var savedScrollOffset by androidx.compose.runtime.mutableStateOf(0)
        private set

    init {
        // Check if user is admin
        scope.launch {
            try {
                val user = getCurrentUser()
                _isAdmin.value = user?.isAdmin == true
            } catch (e: Exception) {
                _isAdmin.value = false
            }
        }
    }

    fun saveScrollPosition(index: Int, offset: Int) {
        savedScrollIndex = index
        savedScrollOffset = offset
    }
}
