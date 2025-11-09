package ireader.domain.services

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.net.toUri
import ireader.domain.models.donation.CryptoType
import ireader.domain.models.donation.WalletApp
import ireader.domain.models.donation.WalletIntegrationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation of WalletIntegrationManager
 * Handles deep linking to wallet apps and clipboard operations
 */
class AndroidWalletIntegrationManager(
    private val context: Context
) : WalletIntegrationManager {
    
    override suspend fun openWallet(
        walletApp: WalletApp,
        cryptoType: CryptoType,
        address: String,
        amount: Double?
    ): WalletIntegrationResult = withContext(Dispatchers.Main) {
        try {
            // Check if wallet is installed first
            if (!isWalletInstalled(walletApp)) {
                return@withContext WalletIntegrationResult.WalletNotInstalled(walletApp)
            }
            
            // Generate deep link URI based on wallet app and crypto type
            val uri = generateDeepLinkUri(walletApp, cryptoType, address, amount)
            
            // Create intent to open wallet app
            val intent = Intent(Intent.ACTION_VIEW, uri.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // Try to set package name to ensure correct app opens
                `package` = walletApp.packageName
            }
            
            context.startActivity(intent)
            WalletIntegrationResult.Success
        } catch (e: ActivityNotFoundException) {
            WalletIntegrationResult.WalletNotInstalled(walletApp)
        } catch (e: Exception) {
            WalletIntegrationResult.Error(e.message ?: "Failed to open wallet")
        }
    }
    
    override suspend fun isWalletInstalled(walletApp: WalletApp): Boolean = withContext(Dispatchers.IO) {
        try {
            context.packageManager.getPackageInfo(walletApp.packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    override fun generatePaymentUri(
        cryptoType: CryptoType,
        address: String,
        amount: Double?
    ): String {
        val baseUri = "${cryptoType.uriScheme}:$address"
        return if (amount != null && amount > 0) {
            "$baseUri?amount=$amount"
        } else {
            baseUri
        }
    }
    
    override suspend fun copyToClipboard(address: String) = withContext(Dispatchers.Main) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Crypto Address", address)
        clipboard.setPrimaryClip(clip)
    }
    
    /**
     * Generate wallet-specific deep link URI
     * Each wallet has its own deep link format
     */
    private fun generateDeepLinkUri(
        walletApp: WalletApp,
        cryptoType: CryptoType,
        address: String,
        amount: Double?
    ): String {
        return when (walletApp) {
            WalletApp.TRUST_WALLET -> {
                // Trust Wallet format: trust://send?asset=<SYMBOL>&address=<ADDRESS>
                val asset = when (cryptoType) {
                    CryptoType.BITCOIN -> "BTC"
                    CryptoType.ETHEREUM -> "ETH"
                    CryptoType.LITECOIN -> "LTC"
                }
                val baseUri = "trust://send?asset=$asset&address=$address"
                if (amount != null && amount > 0) {
                    "$baseUri&amount=$amount"
                } else {
                    baseUri
                }
            }
            WalletApp.METAMASK -> {
                // MetaMask format: metamask://send/<ADDRESS>[@<CHAIN_ID>][?value=<AMOUNT>]
                // MetaMask primarily supports Ethereum
                val baseUri = "metamask://send/$address"
                if (amount != null && amount > 0 && cryptoType == CryptoType.ETHEREUM) {
                    // Convert amount to Wei (1 ETH = 10^18 Wei)
                    val weiAmount = (amount * 1_000_000_000_000_000_000).toLong()
                    "$baseUri?value=$weiAmount"
                } else {
                    baseUri
                }
            }
            WalletApp.COINBASE_WALLET -> {
                // Coinbase Wallet format: coinbase://send?address=<ADDRESS>&asset=<SYMBOL>
                val asset = when (cryptoType) {
                    CryptoType.BITCOIN -> "BTC"
                    CryptoType.ETHEREUM -> "ETH"
                    CryptoType.LITECOIN -> "LTC"
                }
                val baseUri = "coinbase://send?address=$address&asset=$asset"
                if (amount != null && amount > 0) {
                    "$baseUri&amount=$amount"
                } else {
                    baseUri
                }
            }
        }
    }
}
