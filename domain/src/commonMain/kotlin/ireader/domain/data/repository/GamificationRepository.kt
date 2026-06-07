package ireader.domain.data.repository

import ireader.domain.models.gamification.AchievementDef
import ireader.domain.models.gamification.AchievementView
import ireader.domain.models.gamification.CheckinResult
import ireader.domain.models.gamification.GamificationProfile
import ireader.domain.models.gamification.OwnedTitle
import ireader.domain.models.gamification.ReadingStatsSnapshot
import ireader.domain.models.gamification.SpiritStoneTxn
import ireader.domain.models.gamification.UnlockedAchievement

/**
 * Gamification backbone — talks to the Supabase reward engine (SECURITY DEFINER RPCs).
 * Every implementation must degrade gracefully when signed-out / no backend (return
 * empty / Result.failure) so the Profile stays usable in local-first mode.
 */
interface GamificationRepository {

    /** Push local cumulative stats; server merges monotonically + evaluates achievements. */
    suspend fun syncReadingStats(snapshot: ReadingStatsSnapshot): Result<List<UnlockedAchievement>>

    /** Re-evaluate achievements for the current user (e.g. on app open). */
    suspend fun evaluate(): Result<List<UnlockedAchievement>>

    suspend fun getProfile(userId: String): Result<GamificationProfile>

    /** Full catalog (public read). */
    suspend fun getAchievementCatalog(): Result<List<AchievementDef>>

    /** Catalog merged with the user's progress for the showcase. */
    suspend fun getAchievements(userId: String): Result<List<AchievementView>>

    suspend fun getOwnedTitles(userId: String): Result<List<OwnedTitle>>

    suspend fun setActiveTitle(titleId: String?): Result<Unit>

    suspend fun checkinDaily(): Result<CheckinResult>

    suspend fun getStoneHistory(userId: String, limit: Int = 50): Result<List<SpiritStoneTxn>>

    /** Spend earned stones on a cosmetic (BADGE or TITLE). Cosmetic-only. */
    suspend fun spendStones(itemType: String, itemId: String, cost: Int): Result<Long>
}
