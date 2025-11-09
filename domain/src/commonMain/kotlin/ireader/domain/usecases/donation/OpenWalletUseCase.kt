package ireader.domain.usecases.donation

import ireader.domain.models.donation.CryptoType
import ireader.domain.models.donation.WalletApp
import ireader.domain.models.donation.WalletIntegrationResult
import ireader.domain.services.WalletIntegrationManager

/**
 * Use case for opening a cryptocurrency wallet app with pre-filled payment information
 */
class OpenWalletUseCase(
    private val walletIntegrationManager: WalletIntegrationManager
) {
    /**
     * Open a wallet app with the specified payment details
     * @param walletApp The wallet app to open
     * @param cryptoType The cryptocurrency type
     * @param address The wallet address to send to
     * @param amount Optional amount to pre-fill
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(
        walletApp: WalletApp,
        cryptoType: CryptoType,
        address: String,
        amount: Double? = null
    ): WalletIntegrationResult {
        return walletIntegrationManager.openWallet(
            walletApp = walletApp,
            cryptoType = cryptoType,
            address = address,
            amount = amount
        )
    }
}
