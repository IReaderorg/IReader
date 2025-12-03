package ireader.presentation.ui.plugins.details.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginMonetization
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Purchase dialog for premium plugins
 * Requirements: 8.1, 8.2, 8.3, 8.4
 */
@Composable
fun PurchaseDialog(
    plugin: PluginInfo,
    onPurchase: () -> Unit,
    onStartTrial: () -> Unit,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val monetization = plugin.manifest.monetization as? PluginMonetization.Premium ?: return
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Purchase ${plugin.manifest.name}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = plugin.manifest.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Divider()
                
                // Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = localizeHelper.localize(Res.string.price_1),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${monetization.currency} ${ireader.presentation.ui.core.utils.toDecimalString(monetization.price, 2)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Trial info
                monetization.trialDays?.let { days ->
                    Text(
                        text = "Try free for $days days before purchasing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = localizeHelper.localize(Res.string.this_purchase_will_be_synced),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onPurchase) {
                Text(localizeHelper.localize(Res.string.purchase))
            }
        },
        dismissButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                monetization.trialDays?.let {
                    TextButton(onClick = onStartTrial) {
                        Text(localizeHelper.localize(Res.string.start_trial))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(localizeHelper.localize(Res.string.cancel))
                }
            }
        }
    )
}
