package ireader.data.challenge

import ireader.core.prefs.PreferenceStore
import ireader.domain.data.repository.ReadingChallengeRepository
import ireader.domain.models.gamification.Milestone
import ireader.domain.models.gamification.ReadingChallenge
import ireader.domain.models.gamification.ReadingChallengeState
import ireader.domain.models.gamification.ChallengeType
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

class ReadingChallengeRepositoryImpl(
    private val prefs: PreferenceStore
) : ReadingChallengeRepository {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _state = MutableStateFlow(ReadingChallengeState())
    private val state: Flow<ReadingChallengeState> = _state.asStateFlow()

    init {
        scope.launch { loadFromPrefs() }
    }

    override fun observeChallenges(): Flow<ReadingChallengeState> = state

    override suspend fun getActiveChallenges(): ReadingChallengeState {
        loadFromPrefs()
        return _state.value
    }

    override suspend fun createDailyGoal(minutes: Long): ReadingChallenge {
        val now = currentTimeToLong()
        val startOfDay = getStartOfDay(now)
        val endOfDay = startOfDay + TimeUnit.DAYS.toMillis(1)

        val challenge = ReadingChallenge(
            id = UUID.randomUUID().toString(),
            type = ChallengeType.DAILY,
            goalMinutes = minutes,
            currentMinutes = 0,
            rewardStones = (minutes / 10).toInt().coerceIn(5, 50),
            isCompleted = false,
            startDate = startOfDay,
            endDate = endOfDay
        )

        saveChallenge("daily", challenge)
        _state.update { it.copy(dailyChallenge = challenge) }
        return challenge
    }

    override suspend fun createWeeklyGoal(minutes: Long): ReadingChallenge {
        val now = currentTimeToLong()
        val startOfWeek = getStartOfWeek(now)
        val endOfWeek = startOfWeek + TimeUnit.DAYS.toMillis(7)

        val challenge = ReadingChallenge(
            id = UUID.randomUUID().toString(),
            type = ChallengeType.WEEKLY,
            goalMinutes = minutes,
            currentMinutes = 0,
            rewardStones = (minutes / 5).toInt().coerceIn(20, 200),
            isCompleted = false,
            startDate = startOfWeek,
            endDate = endOfWeek
        )

        saveChallenge("weekly", challenge)
        _state.update { it.copy(weeklyChallenge = challenge) }
        return challenge
    }

    override suspend fun createMonthlyGoal(minutes: Long): ReadingChallenge {
        val now = currentTimeToLong()
        val startOfMonth = getStartOfMonth(now)
        val endOfMonth = startOfMonth + TimeUnit.DAYS.toMillis(30)

        val challenge = ReadingChallenge(
            id = UUID.randomUUID().toString(),
            type = ChallengeType.MONTHLY,
            goalMinutes = minutes,
            currentMinutes = 0,
            rewardStones = (minutes / 3).toInt().coerceIn(50, 500),
            isCompleted = false,
            startDate = startOfMonth,
            endDate = endOfMonth
        )

        saveChallenge("monthly", challenge)
        _state.update { it.copy(monthlyChallenge = challenge) }
        return challenge
    }

    override suspend fun updateChallengeProgress(minutesRead: Long) {
        val now = currentTimeToLong()
        val current = _state.value

        current.dailyChallenge?.let { challenge ->
            if (!challenge.isCompleted && now in challenge.startDate..challenge.endDate) {
                val updated = challenge.copy(
                    currentMinutes = challenge.currentMinutes + minutesRead,
                    isCompleted = challenge.currentMinutes + minutesRead >= challenge.goalMinutes,
                    completedAt = if (challenge.currentMinutes + minutesRead >= challenge.goalMinutes) now else null
                )
                saveChallenge("daily", updated)
                val completedCount = if (updated.isCompleted && !challenge.isCompleted) {
                    current.challengesCompletedToday + 1
                } else current.challengesCompletedToday
                val stonesEarned = if (updated.isCompleted && !challenge.isCompleted) {
                    current.totalStonesEarnedFromChallenges + updated.rewardStones
                } else current.totalStonesEarnedFromChallenges
                _state.update { it.copy(dailyChallenge = updated, challengesCompletedToday = completedCount, totalStonesEarnedFromChallenges = stonesEarned) }
            }
        }

        current.weeklyChallenge?.let { challenge ->
            if (!challenge.isCompleted && now in challenge.startDate..challenge.endDate) {
                val updated = challenge.copy(
                    currentMinutes = challenge.currentMinutes + minutesRead,
                    isCompleted = challenge.currentMinutes + minutesRead >= challenge.goalMinutes,
                    completedAt = if (challenge.currentMinutes + minutesRead >= challenge.goalMinutes) now else null
                )
                saveChallenge("weekly", updated)
                _state.update { it.copy(weeklyChallenge = updated) }
            }
        }

        current.monthlyChallenge?.let { challenge ->
            if (!challenge.isCompleted && now in challenge.startDate..challenge.endDate) {
                val updated = challenge.copy(
                    currentMinutes = challenge.currentMinutes + minutesRead,
                    isCompleted = challenge.currentMinutes + minutesRead >= challenge.goalMinutes,
                    completedAt = if (challenge.currentMinutes + minutesRead >= challenge.goalMinutes) now else null
                )
                saveChallenge("monthly", updated)
                _state.update { it.copy(monthlyChallenge = updated) }
            }
        }
    }

    override suspend fun getSeenMilestones(): Set<String> {
        return prefs.getStringSet(KEY_SEEN_MILESTONES, emptySet()).get()
    }

    override suspend fun markMilestoneSeen(milestoneId: String) {
        val seen = getSeenMilestones().toMutableSet()
        seen.add(milestoneId)
        prefs.getStringSet(KEY_SEEN_MILESTONES, emptySet()).set(seen)
    }

    override suspend fun getUnseenMilestones(currentStats: ReadingChallengeState): List<Milestone> {
        return emptyList()
    }

    private suspend fun saveChallenge(type: String, challenge: ReadingChallenge) {
        prefs.getLong("${KEY_PREFIX}_goal_${type}", 0).set(challenge.goalMinutes)
        prefs.getLong("${KEY_PREFIX}_current_${type}", 0).set(challenge.currentMinutes)
        prefs.getBoolean("${KEY_PREFIX}_completed_${type}", false).set(challenge.isCompleted)
        prefs.getLong("${KEY_PREFIX}_start_${type}", 0).set(challenge.startDate)
        prefs.getLong("${KEY_PREFIX}_end_${type}", 0).set(challenge.endDate)
        prefs.getLong("${KEY_PREFIX}_completed_at_${type}", 0).set(challenge.completedAt ?: 0L)
        prefs.getInt("${KEY_PREFIX}_reward_${type}", 0).set(challenge.rewardStones)
    }

    private suspend fun loadFromPrefs() {
        val now = currentTimeToLong()

        val daily = loadChallenge("daily", ChallengeType.DAILY, now)
        val weekly = loadChallenge("weekly", ChallengeType.WEEKLY, now)
        val monthly = loadChallenge("monthly", ChallengeType.MONTHLY, now)

        _state.update {
            it.copy(
                dailyChallenge = daily,
                weeklyChallenge = weekly,
                monthlyChallenge = monthly
            )
        }
    }

    private suspend fun loadChallenge(type: String, challengeType: ChallengeType, now: Long): ReadingChallenge? {
        val goal = prefs.getLong("${KEY_PREFIX}_goal_${type}", 0).get()
        if (goal <= 0) return null

        val start = prefs.getLong("${KEY_PREFIX}_start_${type}", 0).get()
        val end = prefs.getLong("${KEY_PREFIX}_end_${type}", 0).get()

        if (now !in start..end) return null

        return ReadingChallenge(
            id = type,
            type = challengeType,
            goalMinutes = goal,
            currentMinutes = prefs.getLong("${KEY_PREFIX}_current_${type}", 0).get(),
            rewardStones = prefs.getInt("${KEY_PREFIX}_reward_${type}", 0).get(),
            isCompleted = prefs.getBoolean("${KEY_PREFIX}_completed_${type}", false).get(),
            completedAt = prefs.getLong("${KEY_PREFIX}_completed_at_${type}", 0).get().takeIf { it > 0 },
            startDate = start,
            endDate = end
        )
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfWeek(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfMonth(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    companion object {
        private const val KEY_PREFIX = "reading_challenge"
        private const val KEY_SEEN_MILESTONES = "seen_milestones"
    }
}
