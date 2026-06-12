package ireader.data.challenge

import ireader.core.prefs.PreferenceStore
import ireader.domain.data.repository.ReadingChallengeRepository
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
import kotlin.random.Random

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
        val challenge = ReadingChallenge(
            id = generateId(),
            type = ChallengeType.DAILY,
            goalMinutes = minutes,
            currentMinutes = 0,
            rewardStones = (minutes / 10).toInt().coerceIn(5, 50),
            isCompleted = false,
            startDate = getStartOfDay(now),
            endDate = getStartOfDay(now) + MILLIS_PER_DAY
        )
        saveChallenge("daily", challenge)
        _state.update { it.copy(dailyChallenge = challenge) }
        return challenge
    }

    override suspend fun createWeeklyGoal(minutes: Long): ReadingChallenge {
        val now = currentTimeToLong()
        val challenge = ReadingChallenge(
            id = generateId(),
            type = ChallengeType.WEEKLY,
            goalMinutes = minutes,
            currentMinutes = 0,
            rewardStones = (minutes / 5).toInt().coerceIn(20, 200),
            isCompleted = false,
            startDate = getStartOfWeek(now),
            endDate = getStartOfWeek(now) + MILLIS_PER_DAY * 7
        )
        saveChallenge("weekly", challenge)
        _state.update { it.copy(weeklyChallenge = challenge) }
        return challenge
    }

    override suspend fun createMonthlyGoal(minutes: Long): ReadingChallenge {
        val now = currentTimeToLong()
        val challenge = ReadingChallenge(
            id = generateId(),
            type = ChallengeType.MONTHLY,
            goalMinutes = minutes,
            currentMinutes = 0,
            rewardStones = (minutes / 3).toInt().coerceIn(50, 500),
            isCompleted = false,
            startDate = getStartOfMonth(now),
            endDate = getStartOfMonth(now) + MILLIS_PER_DAY * 30
        )
        saveChallenge("monthly", challenge)
        _state.update { it.copy(monthlyChallenge = challenge) }
        return challenge
    }

    override suspend fun updateChallengeProgress(minutesRead: Long) {
        val now = currentTimeToLong()
        val current = _state.value

        current.dailyChallenge?.let { ch -> updateSingleChallenge(ch, "daily", now, minutesRead) { updated ->
            _state.update { s ->
                val completedCount = if (updated.isCompleted && !ch.isCompleted) s.challengesCompletedToday + 1 else s.challengesCompletedToday
                val stonesEarned = if (updated.isCompleted && !ch.isCompleted) s.totalStonesEarnedFromChallenges + updated.rewardStones else s.totalStonesEarnedFromChallenges
                s.copy(dailyChallenge = updated, challengesCompletedToday = completedCount, totalStonesEarnedFromChallenges = stonesEarned)
            }
        } }

        current.weeklyChallenge?.let { ch -> updateSingleChallenge(ch, "weekly", now, minutesRead) { updated ->
            _state.update { s -> s.copy(weeklyChallenge = updated) }
        } }

        current.monthlyChallenge?.let { ch -> updateSingleChallenge(ch, "monthly", now, minutesRead) { updated ->
            _state.update { s -> s.copy(monthlyChallenge = updated) }
        } }
    }

    private suspend fun updateSingleChallenge(
        challenge: ReadingChallenge,
        type: String,
        now: Long,
        minutesRead: Long,
        onUpdate: suspend (ReadingChallenge) -> Unit
    ) {
        if (challenge.isCompleted || now !in challenge.startDate..challenge.endDate) return
        val updated = challenge.copy(
            currentMinutes = challenge.currentMinutes + minutesRead,
            isCompleted = challenge.currentMinutes + minutesRead >= challenge.goalMinutes,
            completedAt = if (challenge.currentMinutes + minutesRead >= challenge.goalMinutes) now else null
        )
        saveChallenge(type, updated)
        onUpdate(updated)
    }

    override suspend fun getSeenMilestones(): Set<String> {
        return prefs.getStringSet(KEY_SEEN_MILESTONES, emptySet()).get()
    }

    override suspend fun markMilestoneSeen(milestoneId: String) {
        val seen = getSeenMilestones().toMutableSet()
        seen.add(milestoneId)
        prefs.getStringSet(KEY_SEEN_MILESTONES, emptySet()).set(seen)
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
        _state.update {
            it.copy(
                dailyChallenge = loadChallenge("daily", ChallengeType.DAILY, now),
                weeklyChallenge = loadChallenge("weekly", ChallengeType.WEEKLY, now),
                monthlyChallenge = loadChallenge("monthly", ChallengeType.MONTHLY, now)
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
        return (timestamp / MILLIS_PER_DAY) * MILLIS_PER_DAY
    }

    private fun getStartOfWeek(timestamp: Long): Long {
        val startOfDay = getStartOfDay(timestamp)
        val daysSinceEpoch = (startOfDay / MILLIS_PER_DAY).toInt()
        val mondayOffset = (daysSinceEpoch + 6) % 7
        return startOfDay - mondayOffset * MILLIS_PER_DAY
    }

    private fun getStartOfMonth(timestamp: Long): Long {
        val startOfDay = getStartOfDay(timestamp)
        val daysSinceEpoch = (startOfDay / MILLIS_PER_DAY).toInt()
        val dayOfMonth = daysSinceEpoch % 30
        return startOfDay - dayOfMonth * MILLIS_PER_DAY
    }

    private fun generateId(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return (1..16).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    companion object {
        private const val KEY_PREFIX = "reading_challenge"
        private const val KEY_SEEN_MILESTONES = "seen_milestones"
        private const val MILLIS_PER_DAY = 86_400_000L
    }
}
