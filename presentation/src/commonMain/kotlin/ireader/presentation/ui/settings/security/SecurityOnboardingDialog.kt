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

/**
 * Onboarding dialog that explains security features to users
 */
@Composable
fun SecurityOnboardingDialog(
    onDismiss: () -> Unit,
    onGetStarted: () -> Unit
) {
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
                text = "Security Features",
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
                    text = "Protect your privacy and secure your reading experience with these features:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                SecurityFeatureItem(
                    icon = Icons.Default.Lock,
                    title = "App Lock",
                    description = "Require authentication (PIN, password, or biometric) to open the app. Choose the method that works best for you."
                )
                
                SecurityFeatureItem(
                    icon = Icons.Default.Fingerprint,
                    title = "Biometric Authentication",
                    description = "Use your fingerprint or face recognition for quick and secure access. Your device PIN can be used as a fallback."
                )
                
                SecurityFeatureItem(
                    icon = Icons.Default.Security,
                    title = "Secure Screen",
                    description = "Block screenshots and screen recording to prevent others from capturing your reading content."
                )
                
                SecurityFeatureItem(
                    icon = Icons.Default.BlurOn,
                    title = "Hide Content",
                    description = "Blur library book covers until you tap them. Perfect for reading in public places."
                )
                
                SecurityFeatureItem(
                    icon = Icons.Default.Warning,
                    title = "18+ Source Lock",
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
                                text = "Best Practices",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Text(
                            text = "• Use a strong PIN (6 digits) or password\n" +
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
                Text("Get Started")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Maybe Later")
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
