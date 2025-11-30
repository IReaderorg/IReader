package ireader.presentation.ui.settings.security

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Help
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.security.AuthMethod
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.SetupSettingComponents
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

@Composable
fun SecuritySettingsScreen(
    vm: SecuritySettingsViewModel,
    padding: PaddingValues
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val appLockEnabled by vm.appLockEnabled
    val secureScreenEnabled by vm.secureScreenEnabled
    val hideContentEnabled by vm.hideContentEnabled
    val adultSourceLockEnabled by vm.adultSourceLockEnabled
    val biometricEnabled by vm.biometricEnabled
    
    val items = buildList<Components> {
        // App Lock Section
        add(Components.Header(
            text = localizeHelper.localize(Res.string.app_lock),
            icon = Icons.Default.Lock
        ))
        add(Components.Switch(
            preference = vm.appLockEnabled,
            title = localizeHelper.localize(Res.string.enable_app_lock),
            subtitle = localizeHelper.localize(Res.string.require_authentication_to_open_the_app),
            icon = Icons.Default.Lock,
            onValue = { vm.toggleAppLock(it) }
        ))
        
        // Show auth method options only if app lock is enabled
        if (appLockEnabled) {
            add(Components.Row(
                title = localizeHelper.localize(Res.string.use_pin),
                subtitle = localizeHelper.localize(Res.string.protect_with_a_4_6_digit_pin),
                icon = Icons.Default.Pin,
                onClick = { vm.showSetupDialog(AuthMethod.PIN("")) }
            ))
        }
        
        if (appLockEnabled) {
            add(Components.Row(
                title = localizeHelper.localize(Res.string.use_password),
                subtitle = localizeHelper.localize(Res.string.protect_with_a_password),
                icon = Icons.Default.Password,
                onClick = { vm.showSetupDialog(AuthMethod.Password("")) }
            ))
        }
        
        if (appLockEnabled && vm.isBiometricAvailable) {
            add(Components.Switch(
                preference = vm.biometricEnabled,
                title = localizeHelper.localize(Res.string.use_biometric),
                subtitle = localizeHelper.localize(Res.string.unlock_with_fingerprint_or_face),
                icon = Icons.Default.Fingerprint,
                onValue = { 
                    if (it) {
                        vm.setupAuthMethod(AuthMethod.Biometric)
                    } else {
                        vm.toggleAppLock(false)
                    }
                }
            ))
        }
        
        add(Components.Space)
        
        // Privacy Section
        add(Components.Header(
            text = localizeHelper.localize(Res.string.privacy),
            icon = Icons.Default.VisibilityOff
        ))
        add(Components.Switch(
            preference = vm.secureScreenEnabled,
            title = localizeHelper.localize(Res.string.secure_screen),
            subtitle = localizeHelper.localize(Res.string.block_screenshots_and_screen_recording),
            icon = Icons.Default.Security,
            onValue = { vm.toggleSecureScreen(it) }
        ))
        add(Components.Switch(
            preference = vm.hideContentEnabled,
            title = localizeHelper.localize(Res.string.hide_content),
            subtitle = localizeHelper.localize(Res.string.blur_library_covers_until_tapped),
            icon = Icons.Default.BlurOn,
            onValue = { vm.toggleHideContent(it) }
        ))
        add(Components.Switch(
            preference = vm.adultSourceLockEnabled,
            title = localizeHelper.localize(Res.string.eighteen_source_lock),
            subtitle = localizeHelper.localize(Res.string.require_authentication_for_adult_sources),
            icon = Icons.Default.Lock,
            onValue = { vm.toggleAdultSourceLock(it) }
        ))
        
        add(Components.Space)
        
        // Information Section
        add(Components.Header(
            text = localizeHelper.localize(Res.string.information),
            icon = Icons.Default.Info
        ))
        add(Components.Row(
            title = localizeHelper.localize(Res.string.security_guide),
            subtitle = localizeHelper.localize(Res.string.learn_about_security_features_and_best_practices),
            icon = Icons.Default.Help,
            onClick = { vm.showOnboarding() }
        ))
        add(Components.Dynamic {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                ),
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = localizeHelper.localize(Res.string.security_best_practices),
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    androidx.compose.material3.Text(
                        text = localizeHelper.localize(Res.string.use_a_strong_pin_or),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        })
    }

    SetupSettingComponents(scaffoldPadding = padding, items = items)
    
    // Show setup dialog when needed
    if (vm.showSetupDialog) {
        SetupAuthDialog(
            authType = vm.setupDialogType,
            onConfirm = { vm.setupAuthMethod(it) },
            onDismiss = { vm.hideSetupDialog() },
            errorMessage = vm.errorMessage
        )
    }
    
    // Show onboarding dialog when requested
    if (vm.showOnboardingDialog) {
        SecurityOnboardingDialog(
            onDismiss = { vm.hideOnboarding() },
            onGetStarted = { vm.hideOnboarding() }
        )
    }
}
