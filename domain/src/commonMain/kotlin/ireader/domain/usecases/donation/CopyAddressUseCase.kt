package ireader.domain.usecases.donation

import ireader.domain.services.WalletIntegrationManager

/**
 * Use case for copying a cryptocurrency address to the clipboard
 */
class CopyAddressUseCase(
    private val walletIntegrationManager: WalletIntegrationManager
) {
    /**
     * Copy an address to the clipboard
     * @param address The address to copy
     */
    suspend operator fun invoke(address: String) {
        walletIntegrationManager.copyToClipboard(address)
    }
}
