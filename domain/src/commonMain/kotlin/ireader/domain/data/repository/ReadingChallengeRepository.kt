package ireader.domain.data.repository

import ireader.domain.models.gamification.ReadingChallenge
import ireader.domain.models.gamification.ReadingChallengeState
import kotlinx.coroutines.flow.Flow

/**
 * Repository for reading challenges and milestones.
 */
interface ReadingChallengeRepository {
    fun observeChallenges(): Flow<ReadingChallengeState>
    suspend fun getActiveChallenges(): ReadingChallengeState
    suspend fun createDailyGoal(minutes: Long): ReadingChallenge
    suspend fun createWeeklyGoal(minutes: Long): ReadingChallenge
    suspend fun createMonthlyGoal(minutes: Long): ReadingChallenge
    suspend fun updateChallengeProgress(minutesRead: Long)
    suspend fun getSeenMilestones(): Set<String>
    suspend fun markMilestoneSeen(milestoneId: String)
}
