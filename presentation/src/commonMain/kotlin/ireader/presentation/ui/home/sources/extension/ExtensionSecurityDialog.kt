package ireader.presentation.ui.home.sources.extension

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.ExtensionSecurity
import ireader.domain.models.entities.ExtensionTrustLevel
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Dialog showing extension security information
 */
@Composable
fun ExtensionSecurityDialog(
    security: ExtensionSecurity,
    extensionName: String,
    onDismiss: () -> Unit,
    onTrustExtension: () -> Unit,
    onBlockExtension: () -> Unit,
) {
    val localizeHelper = LocalLocalizeHelper.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (security.trustLevel) {
                        ExtensionTrustLevel.TRUSTED -> Icons.Default.Verified
                        ExtensionTrustLevel.VERIFIED -> Icons.Default.CheckCircle
                        ExtensionTrustLevel.UNTRUSTED -> Icons.Default.Warning
                        ExtensionTrustLevel.BLOCKED -> Icons.Default.Block
                    },
                    contentDescription = null,
                    tint = when (security.trustLevel) {
                        ExtensionTrustLevel.TRUSTED -> MaterialTheme.colorScheme.primary
                        ExtensionTrustLevel.VERIFIED -> MaterialTheme.colorScheme.tertiary
                        ExtensionTrustLevel.UNTRUSTED -> MaterialTheme.colorScheme.error
                        ExtensionTrustLevel.BLOCKED -> MaterialTheme.colorScheme.error
                    }
                )
                Text("Security: $extensionName")
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Trust level
                item {
                    SecurityInfoItem(
                        title = "Trust Level",
                        value = security.trustLevel.name,
                        icon = Icons.Default.Security
                    )
                }
                
                // Signature
                if (security.signatureHash != null) {
                    item {
                        SecurityInfoItem(
                            title = "Signature Hash",
                            value = security.signatureHash.take(16) + "...",
                            icon = Icons.Default.Fingerprint
                        )
                    }
                }
                
                // Permissions
                item {
                    Text(
                        text = "Permissions",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                items(security.permissions) { permission ->
                    Row(
                        modifier = Modifier.padding(start = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = permission,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                // Security warnings
                if (security.securityWarnings.isNotEmpty()) {
                    item {
                        Text(
                            text = "Security Warnings",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    items(security.securityWarnings) { warning ->
                        Row(
                            modifier = Modifier.padding(start = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = warning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (security.trustLevel != ExtensionTrustLevel.TRUSTED) {
                TextButton(onClick = onTrustExtension) {
                    Text("Trust")
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (security.trustLevel != ExtensionTrustLevel.BLOCKED) {
                    TextButton(
                        onClick = onBlockExtension,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Block")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
private fun SecurityInfoItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
