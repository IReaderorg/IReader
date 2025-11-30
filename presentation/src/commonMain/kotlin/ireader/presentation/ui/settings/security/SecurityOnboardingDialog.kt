package ireader.presentation.ui.settings.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Onboarding dialog that explains security features to users
 */
@Composable
fun SecurityOnboardingDialog(
    onDismiss: () -> Unit,
    onGetStarted: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = localizeHelper.localize(Res.string.security_features),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = localizeHelper.localize(Res.string.protect_your_privacy_and_secure),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                SecurityFeatureItem(
                    icon = Icons.Default.Lock,
                    title = localizeHelper.localize(Res.string.app_lock),
                    description = "Require authentication (PIN, password, or biometric) to open the app. Choose the method that works best for you."
                )
                
                SecurityFeatureItem(
                    icon = Icons.Default.Fingerprint,
                    title = localizeHelper.localize(Res.string.biometric_authentication),
                    description = "Use your fingerprint or face recognition for quick and secure access. Your device PIN can be used as a fallback."
                )
                
                SecurityFeatureItem(
                    icon = Icons.Default.Security,
                    title = localizeHelper.localize(Res.string.secure_screen),
                    description = "Block screenshots and screen recording to prevent others from capturing your reading content."
                )
                
                SecurityFeatureItem(
                    icon = Icons.Default.BlurOn,
                    title = localizeHelper.localize(Res.string.hide_content),
                    description = "Blur library book covers until you tap them. Perfect for reading in public places."
                )
                
                SecurityFeatureItem(
                    icon = Icons.Default.Warning,
                    title = localizeHelper.localize(Res.string.eighteen_source_lock),
                    description = "Require authentication to access adult content sources. Adds an extra layer of privacy protection."
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = localizeHelper.localize(Res.string.best_practices),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Text(
                            text = localizeHelper.localize(Res.string.use_a_strong_pin_6_digits_or_passwordn) +
                                   "• Enable biometric for convenience\n" +
                                   "• Turn on secure screen in public\n" +
                                   "• Use hide content for extra privacy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onGetStarted) {
                Text(localizeHelper.localize(Res.string.get_started))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.maybe_later))
            }
        }
    )
}

@Composable
private fun SecurityFeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
