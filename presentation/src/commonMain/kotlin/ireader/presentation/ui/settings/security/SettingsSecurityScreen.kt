package ireader.presentation.ui.settings.security

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
import ireader.domain.models.prefs.PreferenceValues
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.components.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Enhanced security and privacy settings screen following Mihon's SecurityPreferences.
 * Provides app lock, biometric authentication, secure screen protection, and incognito mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSecurityScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    viewModel: SettingsSecurityViewModel,
    scaffoldPaddingValues: PaddingValues = PaddingValues()
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val listState = rememberSaveable(
        key = "settings_security_scroll_state",
        saver = LazyListState.Saver
    ) {
        LazyListState()
    }

    // Security preferences state
    val appLockEnabled by viewModel.appLockEnabled.collectAsState()
    val appLockMethod by viewModel.appLockMethod.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val lockAfterInactivity by viewModel.lockAfterInactivity.collectAsState()
    val secureScreenMode by viewModel.secureScreenMode.collectAsState()
    val hideNotificationContent by viewModel.hideNotificationContent.collectAsState()
    val incognitoMode by viewModel.incognitoMode.collectAsState()
    val adultContentLock by viewModel.adultContentLock.collectAsState()

    IScaffold(
        modifier = modifier,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = "Security & Privacy",
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
            // App Lock Section
            item {
                SettingsSectionHeader(
                    title = "App Lock",
                    icon = Icons.Outlined.Lock
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = "Enable App Lock",
                    description = "Require authentication to open the app",
                    icon = Icons.Outlined.Lock,
                    checked = appLockEnabled,
                    onCheckedChange = viewModel::setAppLockEnabled
                )
            }
            
            item {
                SettingsItemWithTrailing(
                    title = "Lock Method",
                    description = "Choose authentication method",
                    icon = Icons.Outlined.Security,
                    onClick = { viewModel.showLockMethodDialog() },
                    enabled = appLockEnabled
                ) {
                    Text(
                        text = when (appLockMethod) {
                            "pin" -> "PIN"
                            "password" -> "Password"
                            "pattern" -> "Pattern"
                            "biometric" -> "Biometric"
                            else -> "None"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (appLockEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }
            
            item {
                SettingsSwitchItem(
                    title = "Biometric Authentication",
                    description = "Use fingerprint or face unlock when available",
                    icon = Icons.Outlined.Fingerprint,
                    checked = biometricEnabled,
                    onCheckedChange = viewModel::setBiometricEnabled,
                    enabled = appLockEnabled && viewModel.isBiometricAvailable
                )
            }
            
            item {
                SettingsItemWithTrailing(
                    title = "Lock After Inactivity",
                    description = "Automatically lock app after period of inactivity",
                    icon = Icons.Outlined.Timer,
                    onClick = { viewModel.showLockAfterInactivityDialog() },
                    enabled = appLockEnabled
                ) {
                    Text(
                        text = when (lockAfterInactivity) {
                            0 -> "Immediately"
                            1 -> "1 minute"
                            2 -> "2 minutes"
                            5 -> "5 minutes"
                            10 -> "10 minutes"
                            15 -> "15 minutes"
                            30 -> "30 minutes"
                            60 -> "1 hour"
                            -1 -> "Never"
                            else -> "5 minutes"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (appLockEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }
            
            // Screen Security Section
            item {
                SettingsSectionHeader(
                    title = "Screen Security",
                    icon = Icons.Outlined.ScreenLockPortrait
                )
            }
            
            item {
                SettingsItemWithTrailing(
                    title = "Secure Screen",
                    description = "Hide app content in recent apps and prevent screenshots",
                    icon = Icons.Outlined.ScreenLockPortrait,
                    onClick = { viewModel.showSecureScreenModeDialog() }
                ) {
                    Text(
                        text = when (secureScreenMode) {
                            PreferenceValues.SecureScreenMode.ALWAYS -> "Always"
                            PreferenceValues.SecureScreenMode.INCOGNITO -> "Incognito Only"
                            PreferenceValues.SecureScreenMode.NEVER -> "Never"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Privacy Section
            item {
                SettingsSectionHeader(
                    title = "Privacy",
                    icon = Icons.Outlined.VisibilityOff
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = "Hide Notification Content",
                    description = "Hide book titles and chapter names in notifications",
                    icon = Icons.Outlined.NotificationsOff,
                    checked = hideNotificationContent,
                    onCheckedChange = viewModel::setHideNotificationContent
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = "Incognito Mode",
                    description = "Pause reading history and hide from recent apps",
                    icon = Icons.Outlined.VisibilityOff,
                    checked = incognitoMode,
                    onCheckedChange = viewModel::setIncognitoMode
                )
            }
            
            // Content Restrictions Section
            item {
                SettingsSectionHeader(
                    title = "Content Restrictions",
                    icon = Icons.Outlined.Block
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = "Adult Content Lock",
                    description = "Require authentication to access adult content sources",
                    icon = Icons.Outlined.Block,
                    checked = adultContentLock,
                    onCheckedChange = viewModel::setAdultContentLock
                )
            }
            
            // Advanced Security Section
            item {
                SettingsSectionHeader(
                    title = "Advanced Security",
                    icon = Icons.Outlined.AdminPanelSettings
                )
            }
            
            item {
                SettingsItem(
                    title = "Clear Authentication Data",
                    description = "Remove all saved authentication tokens and sessions",
                    icon = Icons.Outlined.ClearAll,
                    onClick = { viewModel.showClearAuthDataDialog() }
                )
            }
            
            item {
                SettingsItem(
                    title = "Security Audit",
                    description = "Review security settings and recommendations",
                    icon = Icons.Outlined.Security,
                    onClick = { viewModel.navigateToSecurityAudit() }
                )
            }
        }
    }
    
    // Lock Method Dialog
    if (viewModel.showLockMethodDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLockMethodDialog() },
            title = { Text(localizeHelper.localize(Res.string.lock_method)) },
            text = {
                Column {
                    val methods = listOf(
                        "pin" to "PIN",
                        "password" to "Password",
                        "pattern" to "Pattern"
                    )
                    if (viewModel.isBiometricAvailable) {
                        methods + ("biometric" to "Biometric")
                    }
                    methods.forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = appLockMethod == value,
                                onClick = { 
                                    viewModel.setAppLockMethod(value)
                                    viewModel.dismissLockMethodDialog()
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
                TextButton(onClick = { viewModel.dismissLockMethodDialog() }) {
                    Text(localizeHelper.localize(Res.string.ok))
                }
            }
        )
    }
    
    // Lock After Inactivity Dialog
    if (viewModel.showLockAfterInactivityDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLockAfterInactivityDialog() },
            title = { Text(localizeHelper.localize(Res.string.lock_after_inactivity)) },
            text = {
                Column {
                    val options = listOf(
                        0 to "Immediately",
                        1 to "1 minute",
                        2 to "2 minutes",
                        5 to "5 minutes",
                        10 to "10 minutes",
                        15 to "15 minutes",
                        30 to "30 minutes",
                        60 to "1 hour",
                        -1 to "Never"
                    )
                    options.forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = lockAfterInactivity == value,
                                onClick = { 
                                    viewModel.setLockAfterInactivity(value)
                                    viewModel.dismissLockAfterInactivityDialog()
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
                TextButton(onClick = { viewModel.dismissLockAfterInactivityDialog() }) {
                    Text(localizeHelper.localize(Res.string.ok))
                }
            }
        )
    }
    
    // Secure Screen Mode Dialog
    if (viewModel.showSecureScreenModeDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSecureScreenModeDialog() },
            title = { Text(localizeHelper.localize(Res.string.secure_screen)) },
            text = {
                Column {
                    PreferenceValues.SecureScreenMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = secureScreenMode == mode,
                                onClick = { 
                                    viewModel.setSecureScreenMode(mode)
                                    viewModel.dismissSecureScreenModeDialog()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                            ) {
                                Text(
                                    text = when (mode) {
                                        PreferenceValues.SecureScreenMode.ALWAYS -> "Always"
                                        PreferenceValues.SecureScreenMode.INCOGNITO -> "Incognito Only"
                                        PreferenceValues.SecureScreenMode.NEVER -> "Never"
                                    }
                                )
                                Text(
                                    text = when (mode) {
                                        PreferenceValues.SecureScreenMode.ALWAYS -> "Always hide content"
                                        PreferenceValues.SecureScreenMode.INCOGNITO -> "Hide only in incognito mode"
                                        PreferenceValues.SecureScreenMode.NEVER -> "Never hide content"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissSecureScreenModeDialog() }) {
                    Text(localizeHelper.localize(Res.string.ok))
                }
            }
        )
    }
    
    // Clear Auth Data Confirmation Dialog
    if (viewModel.showClearAuthDataDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClearAuthDataDialog() },
            title = { Text(localizeHelper.localize(Res.string.clear_authentication_data)) },
            text = {
                Text(localizeHelper.localize(Res.string.clear_auth_data_confirmation))
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.clearAuthenticationData()
                        viewModel.dismissClearAuthDataDialog()
                    }
                ) {
                    Text(localizeHelper.localize(Res.string.clear_1))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissClearAuthDataDialog() }) {
                    Text(localizeHelper.localize(Res.string.cancel))
                }
            }
        )
    }
}