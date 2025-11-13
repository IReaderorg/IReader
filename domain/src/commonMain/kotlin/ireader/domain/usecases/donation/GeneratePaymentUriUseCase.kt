package ireader.domain.usecases.donation

import ireader.domain.models.donation.CryptoType
import ireader.domain.services.WalletIntegrationManager

/**
 * Use case for generating a payment URI for a cryptocurrency address
 */
class GeneratePaymentUriUseCase(
    private val walletIntegrationManager: WalletIntegrationManager
) {
    /**
     * Generate a payment URI
     * @param address The wallet address
     * @param amount Optional amount to include in URI
     * @param token Optional token contract address
     * @return Payment URI string
     */
    operator fun invoke(
        address: String,
        amount: String? = null,
        token: String? = null
    ): String {
        return walletIntegrationManager.generatePaymentUri(
            address = address,
            amount = amount,
            token = token
        )
    }
}
