package ireader.domain.data.repository

import ireader.domain.models.donation.FundingGoal

/**
 * Repository interface for managing funding goals
 * 
 * This can be implemented to fetch from:
 * - Firebase Remote Config
 * - Supabase database
 * - REST API
 * - Local configuration file
 */
interface FundingGoalRepository {
    
    /**
     * Fetch all active funding goals
     * 
     * @return List of funding goals
     */
    suspend fun getFundingGoals(): Result<List<FundingGoal>>
    
    /**
     * Update a funding goal's current amount
     * 
     * @param goalId The ID of the goal to update
     * @param newAmount The new current amount
     * @return Updated funding goal
     */
    suspend fun updateGoalProgress(goalId: String, newAmount: Double): Result<FundingGoal>
    
    /**
     * Create a new funding goal
     * 
     * @param goal The goal to create
     * @return Created funding goal
     */
    suspend fun createGoal(goal: FundingGoal): Result<FundingGoal>
    
    /**
     * Delete a funding goal
     * 
     * @param goalId The ID of the goal to delete
     * @return Success or failure
     */
    suspend fun deleteGoal(goalId: String): Result<Unit>
    
    /**
     * Rollover a recurring goal to the next period
     * 
     * @param goalId The ID of the recurring goal
     * @return New goal with reset progress
     */
    suspend fun rolloverRecurringGoal(goalId: String): Result<FundingGoal>
}
