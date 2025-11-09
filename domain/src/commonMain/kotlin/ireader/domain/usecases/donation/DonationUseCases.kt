package ireader.domain.usecases.donation

/**
 * Container class for all donation-related use cases
 * Provides a single point of access for donation functionality
 */
data class DonationUseCases(
    val donationTriggerManager: DonationTriggerManager,
    val openWallet: OpenWalletUseCase,
    val checkWalletInstalled: CheckWalletInstalledUseCase,
    val copyAddress: CopyAddressUseCase,
    val generatePaymentUri: GeneratePaymentUriUseCase
)
