package ireader.domain.services

import ireader.domain.models.donation.CryptoType
import ireader.domain.models.donation.WalletApp
import ireader.domain.models.donation.WalletIntegrationResult

/**
 * Interface for managing cryptocurrency wallet integration
 * Platform-specific implementations handle deep linking and wallet app detection
 */
interface WalletIntegrationManager {
    /**
     * Open a wallet app with pre-filled payment information
     * @param walletApp The wallet app to open
     * @param cryptoType The cryptocurrency type
     * @param address The wallet address to send to
     * @param amount Optional amount to pre-fill (not supported by all wallets)
     * @return Result indicating success or failure
     */
    suspend fun openWallet(
        walletApp: WalletApp,
        cryptoType: CryptoType,
        address: String,
        amount: Double? = null
    ): WalletIntegrationResult
    
    /**
     * Check if a wallet app is installed on the device
     * @param walletApp The wallet app to check
     * @return true if installed, false otherwise
     */
    suspend fun isWalletInstalled(walletApp: WalletApp): Boolean
    
    /**
     * Generate a payment URI for a cryptocurrency address
     * Format: <scheme>:<address>[?amount=<amount>]
     * @param cryptoType The cryptocurrency type
     * @param address The wallet address
     * @param amount Optional amount to include in URI
     * @return Payment URI string
     */
    fun generatePaymentUri(
        cryptoType: CryptoType,
        address: String,
        amount: Double? = null
    ): String
    
    /**
     * Copy address to clipboard
     * @param address The address to copy
     */
    suspend fun copyToClipboard(address: String)
    
    /**
     * Request a signature from the user's wallet for authentication
     * @param walletAddress The wallet address to sign with
     * @param message The message to sign
     * @return The signature string, or null if the user cancelled or an error occurred
     */
    suspend fun requestSignature(walletAddress: String, message: String): String?
    
    /**
     * Get the current wallet address
     * On desktop, this returns the address from the stored key pair
     * On mobile, this would return the address from the connected wallet
     * @return The wallet address, or null if no wallet is available
     */
    suspend fun getWalletAddress(): String?
}
