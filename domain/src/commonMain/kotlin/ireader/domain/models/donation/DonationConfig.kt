package ireader.domain.models.donation

/**
 * Configuration for donation addresses and settings
 */
data class DonationConfig(
    val wallets: Map<CryptoType, String>,
    val fundingGoals: List<FundingGoal> = emptyList()
) {
    companion object {
        /**
         * Default donation configuration
         * These addresses should be replaced with actual wallet addresses
         */
        val DEFAULT = DonationConfig(
            wallets = mapOf(
                CryptoType.BITCOIN to "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
                CryptoType.ETHEREUM to "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
                CryptoType.LITECOIN to "LM2WMpR1Rp6j3Sa59cMXMs1SPGKhGGZ1Jz"
            )
        )
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
