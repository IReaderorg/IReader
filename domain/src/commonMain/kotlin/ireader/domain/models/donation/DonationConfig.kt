package ireader.domain.models.donation

/**
 * Configuration for donation settings
 * All payments are now handled through Reymit (supports both card and crypto)
 */
data class DonationConfig(
    val reymitUrl: String = REYMIT_URL,
    val fundingGoals: List<FundingGoal> = emptyList()
) {
    companion object {
        const val REYMIT_URL = "https://reymit.ir/kazemcodes"
        
        val DEFAULT = DonationConfig()
    }
}

/**
 * Represents a funding goal for a specific feature or expense
 */
data class FundingGoal(
    val id: String,
    val title: String,
    val description: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val currency: String = "USD",
    val isRecurring: Boolean = false
) {
    /**
     * Calculate progress percentage
     */
    val progressPercent: Int
        get() = ((currentAmount / targetAmount) * 100).toInt().coerceIn(0, 100)
    
    /**
     * Check if goal is reached
     */
    val isReached: Boolean
        get() = currentAmount >= targetAmount
}
