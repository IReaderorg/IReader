package ireader.domain.usecases.donation

import ireader.domain.data.repository.FundingGoalRepository
import ireader.domain.models.donation.FundingGoal

/**
 * Use case for retrieving current funding goals
 * 
 * Fetches funding goals from the repository, which can be:
 * - Remote config (Firebase Remote Config, Supabase, etc.)
 * - Local configuration file
 * - Hardcoded defaults
 */
class GetFundingGoalsUseCase(
    private val repository: FundingGoalRepository
) {
    
    /**
     * Get all active funding goals
     * 
     * @return List of funding goals to display
     */
    suspend operator fun invoke(): List<FundingGoal> {
        return repository.getFundingGoals().getOrElse {
            // If repository fails, return empty list
            emptyList()
        }
    }
}
