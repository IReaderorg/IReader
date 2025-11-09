package ireader.domain.models.donation

/**
 * Sealed class representing the result of wallet integration operations
 */
sealed class WalletIntegrationResult {
    /**
     * Wallet app opened successfully
     */
    object Success : WalletIntegrationResult()
    
    /**
     * Wallet app is not installed on the device
     * @param walletApp The wallet app that was not found
     */
    data class WalletNotInstalled(val walletApp: WalletApp) : WalletIntegrationResult()
    
    /**
     * Failed to open wallet app due to an error
     * @param error The error message
     */
    data class Error(val error: String) : WalletIntegrationResult()
}
