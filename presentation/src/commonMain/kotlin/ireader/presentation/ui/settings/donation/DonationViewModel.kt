package ireader.presentation.ui.settings.donation

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import ireader.domain.models.donation.CryptoType
import ireader.domain.models.donation.FundingGoal
import ireader.domain.models.donation.WalletApp
import ireader.domain.models.donation.WalletIntegrationResult
import ireader.domain.usecases.donation.DonationUseCases
import ireader.domain.usecases.donation.GetFundingGoalsUseCase
import kotlinx.coroutines.launch

/**
 * ViewModel for the DonationScreen
 * Manages funding goals, wallet integration, and donation-related state
 */
class DonationViewModel(
    private val getFundingGoalsUseCase: GetFundingGoalsUseCase,
    private val donationUseCases: DonationUseCases
) : StateScreenModel<DonationViewModel.State>(State()) {
    
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
        screenModelScope.launch {
            mutableState.value = state.value.copy(isLoading = true, error = null)
            
            try {
                val goals = getFundingGoalsUseCase()
                mutableState.value = state.value.copy(
                    fundingGoals = goals,
                    isLoading = false
                )
            } catch (e: Exception) {
                mutableState.value = state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load funding goals"
                )
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
        cryptoType: CryptoType,
        address: String,
        amount: Double? = null
    ) {
        screenModelScope.launch {
            val result = donationUseCases.openWallet(
                walletApp = walletApp,
                cryptoType = cryptoType,
                address = address,
                amount = amount,
            )
            
            handleWalletResult(result, walletApp)
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
        screenModelScope.launch {
            donationUseCases.copyAddress(address)
            mutableState.value = state.value.copy(
                walletOperationMessage = "$cryptoName address copied to clipboard"
            )
        }
    }
    
    /**
     * Clear wallet operation message
     */
    fun clearWalletMessage() {
        mutableState.value = state.value.copy(walletOperationMessage = null)
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
        
        mutableState.value = state.value.copy(walletOperationMessage = message)
    }
}
