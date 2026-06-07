package ireader.data.gamification

import ireader.domain.data.repository.DiscordShareRepository
import ireader.domain.services.discord.DiscordWebhookService

class DiscordShareRepositoryImpl(
    private val webhookUrl: String,
    private val service: DiscordWebhookService,
) : DiscordShareRepository {

    override val isConfigured: Boolean get() = webhookUrl.isNotBlank()

    override suspend fun shareAchievement(username: String, achievementName: String, tier: String): Result<Unit> =
        service.postMessage("🏅 **@$username** unlocked the **$achievementName** ($tier) achievement on IReader!")

    override suspend fun shareLevelUp(username: String, level: Int, levelTitle: String): Result<Unit> =
        service.postMessage("⭐ **@$username** reached **Level $level — $levelTitle** on IReader!")

    override suspend fun shareStreak(username: String, streakDays: Int): Result<Unit> =
        service.postMessage("🔥 **@$username** is on a **$streakDays-day** reading streak on IReader!")

    override suspend fun shareReview(username: String, bookTitle: String, rating: Int, snippet: String): Result<Unit> {
        val stars = "★".repeat(rating.coerceIn(0, 5)) + "☆".repeat((5 - rating).coerceIn(0, 5))
        return service.postMessage("📝 **@$username** reviewed **$bookTitle** $stars\n> ${snippet.take(280)}")
    }

    override suspend fun shareFavorites(username: String, bookTitles: List<String>): Result<Unit> {
        val list = bookTitles.take(5).joinToString("\n") { "• $it" }
        return service.postMessage("📚 **@$username**'s bookshelf on IReader:\n$list")
    }
}
