package ireader.presentation.ui.settings.donation

import ireader.domain.models.donation.CryptoType
import ireader.domain.models.donation.FundingGoal
import ireader.domain.models.donation.WalletApp
import ireader.domain.models.donation.WalletIntegrationResult
import ireader.domain.usecases.donation.DonationUseCases
import ireader.domain.usecases.donation.GetFundingGoalsUseCase
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.launch

/**
 * ViewModel for the DonationScreen
 * Manages funding goals, wallet integration, and donation-related state
 */
class DonationViewModel(
    private val getFundingGoalsUseCase: GetFundingGoalsUseCase,
    private val donationUseCases: DonationUseCases
) : StateViewModel<DonationViewModel.State>(State()) {
    
    data class State(
        val fundingGoals: List<FundingGoal> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val walletOperationMessage: String? = null
    )
    
    init {
        loadFundingGoals()
    }
    
    /**
     * Load funding goals from repository
     */
    fun loadFundingGoals() {
        scope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            
            try {
                val goals = getFundingGoalsUseCase()
                updateState { it.copy(
                    fundingGoals = goals,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                updateState { it.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load funding goals"
                ) }
            }
        }
    }
    
    /**
     * Refresh funding goals
     */
    fun refresh() {
        loadFundingGoals()
    }
    
    /**
     * Open a cryptocurrency wallet app with pre-filled payment information
     */
    fun openWallet(
        walletApp: WalletApp,
        address: String,
        amount: String? = null
    ) {
        scope.launch {
            // Generate payment URI
            val paymentUri = donationUseCases.generatePaymentUri(
                address = address,
                amount = amount
            )
            
            // Open wallet with payment URI
            val success = donationUseCases.openWallet(
                walletApp = walletApp,
                paymentUri = paymentUri
            )
            
            // Handle result - no logging needed
        }
    }
    
    /**
     * Check if a wallet app is installed on the device
     */
    suspend fun isWalletInstalled(walletApp: WalletApp): Boolean {
        return donationUseCases.checkWalletInstalled(walletApp)
    }
    
    /**
     * Copy cryptocurrency address to clipboard
     */
    fun copyAddress(address: String, cryptoName: String) {
        scope.launch {
            donationUseCases.copyAddress(address)
            updateState { it.copy(
                walletOperationMessage = "$cryptoName address copied to clipboard"
            ) }
        }
    }
    
    /**
     * Clear wallet operation message
     */
    fun clearWalletMessage() {
        updateState { it.copy(walletOperationMessage = null) }
    }
    
    /**
     * Handle wallet integration result
     */
    private fun handleWalletResult(result: WalletIntegrationResult, walletApp: WalletApp) {
        val message = when (result) {
            is WalletIntegrationResult.Success -> {
                "Opening ${walletApp.displayName}..."
            }
            is WalletIntegrationResult.WalletNotInstalled -> {
                "${walletApp.displayName} is not installed. Please install it from your app store."
            }
            is WalletIntegrationResult.Error -> {
                "Failed to open wallet: ${result.error}"
            }
        }
        
        updateState { it.copy(walletOperationMessage = message) }
    }
}
