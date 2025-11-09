package ireader.domain.usecases.donation

import ireader.domain.models.donation.WalletApp
import ireader.domain.services.WalletIntegrationManager

/**
 * Use case for checking if a wallet app is installed on the device
 */
class CheckWalletInstalledUseCase(
    private val walletIntegrationManager: WalletIntegrationManager
) {
    /**
     * Check if a wallet app is installed
     * @param walletApp The wallet app to check
     * @return true if installed, false otherwise
     */
    suspend operator fun invoke(walletApp: WalletApp): Boolean {
        return walletIntegrationManager.isWalletInstalled(walletApp)
    }
}
