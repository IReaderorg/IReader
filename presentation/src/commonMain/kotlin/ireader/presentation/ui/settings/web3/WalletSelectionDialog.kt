package ireader.presentation.ui.settings.web3

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ireader.domain.models.donation.WalletApp

/**
 * Dialog for selecting a wallet to connect
 */
@Composable
fun WalletSelectionDialog(
    onDismiss: () -> Unit,
    onWalletSelected: (WalletApp) -> Unit,
    installedWallets: Set<WalletApp> = emptySet()
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Connect Wallet",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Choose your preferred wallet to connect",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Wallet list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(getAvailableWallets()) { wallet ->
                        WalletOption(
                            wallet = wallet,
                            isInstalled = wallet in installedWallets,
                            onClick = { onWalletSelected(wallet) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Info text
                Text(
                    text = "Don't have a wallet? Install one from the app store.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WalletOption(
    wallet: WalletApp,
    isInstalled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInstalled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Wallet icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        Modifier.then(
                            if (isInstalled) {
                                Modifier
                            } else {
                                Modifier
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getWalletEmoji(wallet),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getWalletName(wallet),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = if (isInstalled) "Installed" else "Not installed",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isInstalled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            if (isInstalled) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                    contentDescription = "Installed",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun getAvailableWallets(): List<WalletApp> {
    return listOf(
        WalletApp.METAMASK,
        WalletApp.TRUST_WALLET,
        WalletApp.RAINBOW,
        WalletApp.COINBASE,
        WalletApp.ARGENT
    )
}

private fun getWalletName(wallet: WalletApp): String {
    return when (wallet) {
        WalletApp.METAMASK -> "MetaMask"
        WalletApp.TRUST_WALLET -> "Trust Wallet"
        WalletApp.RAINBOW -> "Rainbow"
        WalletApp.COINBASE -> "Coinbase Wallet"
        WalletApp.ARGENT -> "Argent"
        else -> wallet.name
    }
}

private fun getWalletEmoji(wallet: WalletApp): String {
    return when (wallet) {
        WalletApp.METAMASK -> "🦊"
        WalletApp.TRUST_WALLET -> "🛡️"
        WalletApp.RAINBOW -> "🌈"
        WalletApp.COINBASE -> "💼"
        WalletApp.ARGENT -> "🔷"
        else -> "💳"
    }
}
