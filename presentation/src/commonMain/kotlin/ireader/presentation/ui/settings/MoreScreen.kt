package ireader.presentation.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import ireader.domain.preferences.prefs.SupabasePreferences
import ireader.domain.preferences.prefs.UiPreferences
import ireader.i18n.Images.incognito
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.LogoHeader
import ireader.presentation.ui.component.components.PreferenceRow
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.components.SettingsItem
import ireader.presentation.ui.settings.components.SettingsSectionHeader
import kotlinx.coroutines.launch
import androidx.compose.runtime.State


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
    onCommunityHub: () -> Unit = {},
    onReadingBuddy: () -> Unit = {},
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }

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
    
    LazyColumn(
        modifier = modifier,
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
        
        // Community Hub Section (only visible when Supabase is enabled)
        if (vm.supabaseEnabled.value) {
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.community),
                    icon = Icons.Filled.People
                )
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.community),
                    description = "Leaderboards, reviews, badges, and more",
                    icon = Icons.Filled.People,
                    onClick = onCommunityHub
                )
            }
        }
        
        // Reading Hub Section
        item {
            SettingsSectionHeader(
                title = "Reading Hub",
                icon = Icons.Filled.Pets
            )
        }
        
        item {
            SettingsItem(
                title = "Reading Hub",
                description = "Statistics, achievements, quotes & your reading buddy",
                icon = Icons.Filled.Pets,
                onClick = onReadingBuddy
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
    supabasePreferences: SupabasePreferences,
    private val getCurrentUser: suspend () -> ireader.domain.models.remote.User?
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {
    val incognitoMode = uiPreferences.incognitoMode().asState()
    
    /**
     * Whether Supabase/cloud features are enabled.
     * When false, community features (leaderboard, reviews, badges) should be hidden.
     */
    val supabaseEnabled = supabasePreferences.supabaseEnabled().asState()
    
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
