package ireader.presentation.ui.settings.donation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ireader.domain.models.donation.CryptoType
import ireader.domain.models.donation.WalletApp
import kotlinx.coroutines.launch

/**
 * Dialog for selecting a cryptocurrency wallet app
 */
@Composable
fun WalletSelectionDialog(
    cryptoType: CryptoType,
    address: String,
    onWalletSelected: (WalletApp) -> Unit,
    onDismiss: () -> Unit,
    checkWalletInstalled: suspend (WalletApp) -> Boolean
) {
    val scope = rememberCoroutineScope()
    var installedWallets by remember { mutableStateOf<Map<WalletApp, Boolean>>(emptyMap()) }
    
    // Check which wallets are installed
    LaunchedEffect(Unit) {
        val wallets = WalletApp.values()
        val installationStatus = wallets.associateWith { wallet ->
            checkWalletInstalled(wallet)
        }
        installedWallets = installationStatus
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Select Wallet App",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Choose a wallet app to open with ${cryptoType.displayName} payment details",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // List of wallet apps
                WalletApp.values().forEach { wallet ->
                    val isInstalled = installedWallets[wallet] ?: false
                    
                    WalletAppItem(
                        walletApp = wallet,
                        isInstalled = isInstalled,
                        onClick = {
                            onWalletSelected(wallet)
                            onDismiss()
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Cancel button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun WalletAppItem(
    walletApp: WalletApp,
    isInstalled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isInstalled) 
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(enabled = true, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = walletApp.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = if (isInstalled) "Installed" else "Not installed - will prompt to install",
                style = MaterialTheme.typography.bodySmall,
                color = if (isInstalled) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (isInstalled) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Installed",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
