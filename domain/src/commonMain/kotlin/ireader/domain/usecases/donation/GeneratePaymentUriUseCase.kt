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
     * @param cryptoType The cryptocurrency type
     * @param address The wallet address
     * @param amount Optional amount to include in URI
     * @return Payment URI string
     */
    operator fun invoke(
        cryptoType: CryptoType,
        address: String,
        amount: Double? = null
    ): String {
        return walletIntegrationManager.generatePaymentUri(
            cryptoType = cryptoType,
            address = address,
            amount = amount
        )
    }
}
