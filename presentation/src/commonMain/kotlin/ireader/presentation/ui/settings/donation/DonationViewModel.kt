package ireader.presentation.ui.settings.donation

import ireader.domain.models.donation.FundingGoal
import ireader.domain.usecases.donation.DonationUseCases
import ireader.domain.usecases.donation.GetFundingGoalsUseCase
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.launch

/**
 * ViewModel for the DonationScreen
 * Manages funding goals and donation-related state
 */
class DonationViewModel(
    private val getFundingGoalsUseCase: GetFundingGoalsUseCase,
    private val donationUseCases: DonationUseCases
) : StateViewModel<DonationViewModel.State>(State()) {
    
    data class State(
        val fundingGoals: List<FundingGoal> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
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
}
