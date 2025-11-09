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

@Composable
fun SecuritySettingsScreen(
    vm: SecuritySettingsViewModel,
    padding: PaddingValues
) {
    val appLockEnabled by vm.appLockEnabled
    val secureScreenEnabled by vm.secureScreenEnabled
    val hideContentEnabled by vm.hideContentEnabled
    val adultSourceLockEnabled by vm.adultSourceLockEnabled
    val biometricEnabled by vm.biometricEnabled
    
    val items = buildList<Components> {
        // App Lock Section
        add(Components.Header(
            text = "App Lock",
            icon = Icons.Default.Lock
        ))
        add(Components.Switch(
            preference = vm.appLockEnabled,
            title = "Enable App Lock",
            subtitle = "Require authentication to open the app",
            icon = Icons.Default.Lock,
            onValue = { vm.toggleAppLock(it) }
        ))
        
        // Show auth method options only if app lock is enabled
        if (appLockEnabled) {
            add(Components.Row(
                title = "Use PIN",
                subtitle = "Protect with a 4-6 digit PIN",
                icon = Icons.Default.Pin,
                onClick = { vm.showSetupDialog(AuthMethod.PIN("")) }
            ))
        }
        
        if (appLockEnabled) {
            add(Components.Row(
                title = "Use Password",
                subtitle = "Protect with a password",
                icon = Icons.Default.Password,
                onClick = { vm.showSetupDialog(AuthMethod.Password("")) }
            ))
        }
        
        if (appLockEnabled && vm.isBiometricAvailable) {
            add(Components.Switch(
                preference = vm.biometricEnabled,
                title = "Use Biometric",
                subtitle = "Unlock with fingerprint or face",
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
            text = "Privacy",
            icon = Icons.Default.VisibilityOff
        ))
        add(Components.Switch(
            preference = vm.secureScreenEnabled,
            title = "Secure Screen",
            subtitle = "Block screenshots and screen recording",
            icon = Icons.Default.Security,
            onValue = { vm.toggleSecureScreen(it) }
        ))
        add(Components.Switch(
            preference = vm.hideContentEnabled,
            title = "Hide Content",
            subtitle = "Blur library covers until tapped",
            icon = Icons.Default.BlurOn,
            onValue = { vm.toggleHideContent(it) }
        ))
        add(Components.Switch(
            preference = vm.adultSourceLockEnabled,
            title = "18+ Source Lock",
            subtitle = "Require authentication for adult sources",
            icon = Icons.Default.Lock,
            onValue = { vm.toggleAdultSourceLock(it) }
        ))
        
        add(Components.Space)
        
        // Information Section
        add(Components.Header(
            text = "Information",
            icon = Icons.Default.Info
        ))
        add(Components.Row(
            title = "Security Guide",
            subtitle = "Learn about security features and best practices",
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
                        text = "Security Best Practices",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    androidx.compose.material3.Text(
                        text = "• Use a strong PIN or password\n• Enable biometric authentication for convenience\n• Enable secure screen in public places\n• Use 18+ source lock for additional privacy",
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
