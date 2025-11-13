package ireader.domain.usecases.donation

import ireader.domain.models.donation.WalletApp
import ireader.domain.services.WalletIntegrationManager

/**
 * Use case for opening a wallet app with a payment URI
 * Used for cryptocurrency donations only
 */
class OpenWalletUseCase(
    private val walletIntegrationManager: WalletIntegrationManager
) {
    /**
     * Open a wallet app with a payment URI
     * @param walletApp The wallet app to open
     * @param paymentUri The payment URI to pass to the wallet
     * @return true if wallet was opened successfully, false otherwise
     */
    suspend operator fun invoke(
        walletApp: WalletApp,
        paymentUri: String
    ): Boolean {
        return walletIntegrationManager.openWallet(walletApp, paymentUri)
    }
}
