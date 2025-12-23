package ireader.presentation.ui.settings.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
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
    
    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

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
    
    // Sync state
    val isSyncing by viewModel.isSyncing.collectAsState()
    val trackedBooksCount by viewModel.trackedBooksCount.collectAsState()
    
    // Show snackbar when message changes
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    IScaffold(
        modifier = modifier,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = localizeHelper.localize(Res.string.tracking),
                popBackStack = onNavigateUp,
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            
            // Statistics item
            item {
                SettingsItemWithTrailing(
                    title = "Tracked Books",
                    description = "Total books being tracked across all services",
                    icon = Icons.Outlined.LibraryBooks,
                    onClick = { },
                    enabled = true
                ) {
                    Text(
                        text = "$trackedBooksCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Last sync time
            item {
                SettingsItemWithTrailing(
                    title = "Last Sync",
                    description = "When tracking data was last synchronized",
                    icon = Icons.Outlined.Schedule,
                    onClick = { },
                    enabled = true
                ) {
                    Text(
                        text = viewModel.getFormattedLastSyncTime(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            item {
                SettingsItem(
                    title = localizeHelper.localize(Res.string.sync_history),
                    description = "View sync history and statistics",
                    icon = Icons.Outlined.History,
                    onClick = { viewModel.navigateToSyncHistory() }
                )
            }
            
            item {
                if (isSyncing) {
                    SettingsItemWithTrailing(
                        title = localizeHelper.localize(Res.string.manual_sync),
                        description = "Syncing in progress...",
                        icon = Icons.Outlined.Sync,
                        onClick = { },
                        enabled = false
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    SettingsItem(
                        title = localizeHelper.localize(Res.string.manual_sync),
                        description = "Force sync all tracked books now",
                        icon = Icons.Outlined.Sync,
                        onClick = { viewModel.performManualSync() }
                    )
                }
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
    
    // AniList Login Dialog
    if (viewModel.showAniListLoginDialog) {
        AniListLoginDialog(
            authUrl = viewModel.aniListAuthUrl.collectAsState().value,
            error = viewModel.aniListLoginError,
            onDismiss = { viewModel.dismissAniListLoginDialog() },
            onTokenSubmit = { token -> viewModel.completeAniListLogin(token) }
        )
    }
    
    // MAL Login Dialog
    if (viewModel.showMalLoginDialog) {
        MalLoginDialog(
            authUrl = viewModel.malAuthUrl.collectAsState().value,
            error = viewModel.malLoginError,
            onDismiss = { viewModel.dismissMalLoginDialog() },
            onCodeSubmit = { code -> viewModel.completeMalLogin(code) }
        )
    }
    
    // Kitsu Login Dialog
    if (viewModel.showKitsuLoginDialog) {
        KitsuLoginDialog(
            error = viewModel.kitsuLoginError,
            onDismiss = { viewModel.dismissKitsuLoginDialog() },
            onLogin = { username, password -> viewModel.completeKitsuLogin(username, password) }
        )
    }
    
    // MangaUpdates Login Dialog
    if (viewModel.showMangaUpdatesLoginDialog) {
        MangaUpdatesLoginDialog(
            error = viewModel.mangaUpdatesLoginError,
            onDismiss = { viewModel.dismissMangaUpdatesLoginDialog() },
            onLogin = { username, password -> viewModel.completeMangaUpdatesLogin(username, password) }
        )
    }
    
    // Sync History Dialog
    if (viewModel.showSyncHistoryDialog) {
        SyncHistoryDialog(
            trackedBooksCount = trackedBooksCount,
            lastSyncTime = viewModel.getFormattedLastSyncTime(),
            aniListLoggedIn = aniListLoggedIn,
            malLoggedIn = malLoggedIn,
            kitsuLoggedIn = kitsuLoggedIn,
            mangaUpdatesLoggedIn = mangaUpdatesLoggedIn,
            onDismiss = { viewModel.dismissSyncHistoryDialog() }
        )
    }
}

/**
 * Dialog for AniList OAuth login.
 * Shows instructions and a button to open browser for authentication.
 * The app will automatically capture the access token via deeplink callback.
 */
@Composable
private fun AniListLoginDialog(
    authUrl: String?,
    error: String?,
    onDismiss: () -> Unit,
    onTokenSubmit: (String) -> Unit,
    onOpenBrowser: () -> Unit = {}
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    var tokenInput by remember { mutableStateOf("") }
    var showManualEntry by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Login to AniList") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!showManualEntry) {
                    // Primary flow: Open browser
                    Text(
                        text = "Click the button below to login to AniList. After authorizing, you'll be redirected back to the app automatically.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Open Browser Button
                    Button(
                        onClick = {
                            authUrl?.let { url ->
                                try {
                                    uriHandler.openUri(url)
                                } catch (e: Exception) {
                                    // Fallback to manual entry if browser fails
                                    showManualEntry = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = authUrl != null
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.OpenInBrowser,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open AniList in Browser")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Manual entry option
                    TextButton(
                        onClick = { showManualEntry = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Having trouble? Enter token manually",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    // Manual entry flow (fallback)
                    Text(
                        text = "To login to AniList manually:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "1. Open the URL below in your browser\n" +
                               "2. Login and authorize IReader\n" +
                               "3. Copy the full URL from the browser after redirect\n" +
                               "4. Paste it below (or just the access token)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (authUrl != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            SelectionContainer {
                                Text(
                                    text = authUrl,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                        
                        // Copy URL button
                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                        TextButton(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(authUrl))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy URL")
                        }
                    }
                    
                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("Access Token or URL") },
                        placeholder = { Text("Paste the full URL or access token") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3,
                        isError = error != null
                    )
                    
                    // Back to browser option
                    TextButton(
                        onClick = { showManualEntry = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "← Back to browser login",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            if (showManualEntry) {
                Button(
                    onClick = { onTokenSubmit(tokenInput) },
                    enabled = tokenInput.isNotBlank()
                ) {
                    Text(localizeHelper.localize(Res.string.login))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}

/**
 * Dialog for MyAnimeList OAuth login.
 * Shows instructions and a text field to paste the authorization code.
 */
@Composable
private fun MalLoginDialog(
    authUrl: String?,
    error: String?,
    onDismiss: () -> Unit,
    onCodeSubmit: (String) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var codeInput by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Login to MyAnimeList") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "To login to MyAnimeList:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "1. Open the URL below in your browser\n" +
                           "2. Login and authorize IReader\n" +
                           "3. Copy the authorization code from the redirect URL\n" +
                           "4. Paste it below",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (authUrl != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        SelectionContainer {
                            Text(
                                text = authUrl,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it },
                    label = { Text("Authorization Code") },
                    placeholder = { Text("Paste your authorization code here") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = error != null
                )
                
                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCodeSubmit(codeInput) },
                enabled = codeInput.isNotBlank()
            ) {
                Text(localizeHelper.localize(Res.string.login))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}

/**
 * Dialog for Kitsu login with username and password.
 */
@Composable
private fun KitsuLoginDialog(
    error: String?,
    onDismiss: () -> Unit,
    onLogin: (String, String) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Login to Kitsu") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Enter your Kitsu credentials:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Email or Username") },
                    placeholder = { Text("Enter your email or username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = error != null
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("Enter your password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) {
                        androidx.compose.ui.text.input.VisualTransformation.None
                    } else {
                        androidx.compose.ui.text.input.PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    isError = error != null
                )
                
                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onLogin(username, password) },
                enabled = username.isNotBlank() && password.isNotBlank()
            ) {
                Text(localizeHelper.localize(Res.string.login))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}

/**
 * Dialog for MangaUpdates login with username and password.
 */
@Composable
private fun MangaUpdatesLoginDialog(
    error: String?,
    onDismiss: () -> Unit,
    onLogin: (String, String) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Login to MangaUpdates") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Enter your MangaUpdates credentials:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    placeholder = { Text("Enter your username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = error != null
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("Enter your password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) {
                        androidx.compose.ui.text.input.VisualTransformation.None
                    } else {
                        androidx.compose.ui.text.input.PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    isError = error != null
                )
                
                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onLogin(username, password) },
                enabled = username.isNotBlank() && password.isNotBlank()
            ) {
                Text(localizeHelper.localize(Res.string.login))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
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

/**
 * Dialog showing sync history and statistics
 */
@Composable
private fun SyncHistoryDialog(
    trackedBooksCount: Int,
    lastSyncTime: String,
    aniListLoggedIn: Boolean,
    malLoggedIn: Boolean,
    kitsuLoggedIn: Boolean,
    mangaUpdatesLoggedIn: Boolean,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localizeHelper.localize(Res.string.sync_history)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Statistics section
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tracked Books",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$trackedBooksCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Last Sync",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = lastSyncTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                HorizontalDivider()
                
                // Services section
                Text(
                    text = "Connected Services",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // AniList
                ServiceStatusRow(
                    icon = Icons.Outlined.Favorite,
                    serviceName = "AniList",
                    isConnected = aniListLoggedIn
                )
                
                // MyAnimeList
                ServiceStatusRow(
                    icon = Icons.Outlined.Star,
                    serviceName = "MyAnimeList",
                    isConnected = malLoggedIn
                )
                
                // Kitsu
                ServiceStatusRow(
                    icon = Icons.Outlined.Pets,
                    serviceName = "Kitsu",
                    isConnected = kitsuLoggedIn
                )
                
                // MangaUpdates
                ServiceStatusRow(
                    icon = Icons.Outlined.Update,
                    serviceName = "MangaUpdates",
                    isConnected = mangaUpdatesLoggedIn
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.ok))
            }
        }
    )
}

@Composable
private fun ServiceStatusRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    serviceName: String,
    isConnected: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isConnected) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = serviceName,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = if (isConnected) "Connected" else "Not connected",
            style = MaterialTheme.typography.bodySmall,
            color = if (isConnected) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
