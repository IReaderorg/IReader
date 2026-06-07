package ireader.domain.data.repository

/**
 * Share-to-Discord (§3 of the community plan). One-tap sharing of recognition moments to the
 * community server via an outbound webhook — no bot, no inbound server. Degrades to a no-op
 * failure when no webhook is configured.
 */
interface DiscordShareRepository {
    val isConfigured: Boolean
    suspend fun shareAchievement(username: String, achievementName: String, tier: String): Result<Unit>
    suspend fun shareLevelUp(username: String, level: Int, levelTitle: String): Result<Unit>
    suspend fun shareStreak(username: String, streakDays: Int): Result<Unit>
    suspend fun shareReview(username: String, bookTitle: String, rating: Int, snippet: String): Result<Unit>
    suspend fun shareFavorites(username: String, bookTitles: List<String>): Result<Unit>
}
