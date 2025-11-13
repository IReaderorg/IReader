package ireader.domain.services

import ireader.domain.models.donation.WalletApp

/**
 * Interface for wallet integration - used only for donation features
 * This is NOT used for authentication
 */
interface WalletIntegrationManager {
    /**
     * Check if a wallet app is installed on the device
     */
    suspend fun isWalletInstalled(walletApp: WalletApp): Boolean
    
    /**
     * Open a wallet app with a payment URI
     */
    suspend fun openWallet(walletApp: WalletApp, paymentUri: String): Boolean
    
    /**
     * Generate a payment URI for cryptocurrency donation
     */
    fun generatePaymentUri(
        address: String,
        amount: String? = null,
        token: String? = null
    ): String
    
    /**
     * Copy text to clipboard
     */
    fun copyToClipboard(text: String, label: String = "")
}
