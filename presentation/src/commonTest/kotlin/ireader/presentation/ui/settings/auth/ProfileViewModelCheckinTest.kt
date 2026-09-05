package ireader.presentation.ui.settings.auth

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.data.repository.GamificationRepository
import ireader.domain.models.gamification.AchievementDef
import ireader.domain.models.gamification.AchievementView
import ireader.domain.models.gamification.CheckinResult
import ireader.domain.models.gamification.GamificationProfile
import ireader.domain.models.gamification.OwnedTitle
import ireader.domain.models.gamification.ReadingStatsSnapshot
import ireader.domain.models.gamification.SpiritStoneTxn
import ireader.domain.models.gamification.UnlockedAchievement
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.domain.utils.extensions.formatIsoDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ProfileViewModelCheckinTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class TestPreferenceStore : PreferenceStore {
        val stringValues = mutableMapOf<String, String>()
        val booleanValues = mutableMapOf<String, Boolean>()
        val intValues = mutableMapOf<String, Int>()
        val longValues = mutableMapOf<String, Long>()
        val floatValues = mutableMapOf<String, Float>()

        override fun getString(key: String, defaultValue: String): Preference<String> {
            return object : Preference<String> {
                override fun key(): String = key
                override fun get(): String = stringValues[key] ?: defaultValue
                override fun set(value: String) { stringValues[key] = value }
                override fun isSet(): Boolean = stringValues.containsKey(key)
                override fun delete() { stringValues.remove(key) }
                override fun defaultValue(): String = defaultValue
                override fun changes(): Flow<String> = MutableStateFlow(get())
                override fun stateIn(scope: CoroutineScope): StateFlow<String> = MutableStateFlow(get())
            }
        }

        override fun getLong(key: String, defaultValue: Long): Preference<Long> {
            return object : Preference<Long> {
                override fun key(): String = key
                override fun get(): Long = longValues[key] ?: defaultValue
                override fun set(value: Long) { longValues[key] = value }
                override fun isSet(): Boolean = longValues.containsKey(key)
                override fun delete() { longValues.remove(key) }
                override fun defaultValue(): Long = defaultValue
                override fun changes(): Flow<Long> = MutableStateFlow(get())
                override fun stateIn(scope: CoroutineScope): StateFlow<Long> = MutableStateFlow(get())
            }
        }

        override fun getInt(key: String, defaultValue: Int): Preference<Int> {
            return object : Preference<Int> {
                override fun key(): String = key
                override fun get(): Int = intValues[key] ?: defaultValue
                override fun set(value: Int) { intValues[key] = value }
                override fun isSet(): Boolean = intValues.containsKey(key)
                override fun delete() { intValues.remove(key) }
                override fun defaultValue(): Int = defaultValue
                override fun changes(): Flow<Int> = MutableStateFlow(get())
                override fun stateIn(scope: CoroutineScope): StateFlow<Int> = MutableStateFlow(get())
            }
        }

        override fun getFloat(key: String, defaultValue: Float): Preference<Float> = throw UnsupportedOperationException()
        override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
            return object : Preference<Boolean> {
                override fun key(): String = key
                override fun get(): Boolean = booleanValues[key] ?: defaultValue
                override fun set(value: Boolean) { booleanValues[key] = value }
                override fun isSet(): Boolean = booleanValues.containsKey(key)
                override fun delete() { booleanValues.remove(key) }
                override fun defaultValue(): Boolean = defaultValue
                override fun changes(): Flow<Boolean> = MutableStateFlow(get())
                override fun stateIn(scope: CoroutineScope): StateFlow<Boolean> = MutableStateFlow(get())
            }
        }

        override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> = throw UnsupportedOperationException()
        override fun <T> getObject(key: String, defaultValue: T, serializer: (T) -> String, deserializer: (String) -> T): Preference<T> = throw UnsupportedOperationException()
        override fun <T> getJsonObject(key: String, defaultValue: T, serializer: KSerializer<T>, serializersModule: SerializersModule): Preference<T> = throw UnsupportedOperationException()
    }

    private class FakeGamificationRepository(
        var checkinResult: Result<CheckinResult> = Result.success(CheckinResult(already = false, streakDay = 1, reward = 10)),
        var profileResult: Result<GamificationProfile> = Result.success(GamificationProfile(userId = "test_user", spiritStones = 10, checkinStreak = 1))
    ) : GamificationRepository {
        override suspend fun syncReadingStats(snapshot: ReadingStatsSnapshot) = Result.success(emptyList<UnlockedAchievement>())
        override suspend fun evaluate() = Result.success(emptyList<UnlockedAchievement>())
        override suspend fun getProfile(userId: String) = profileResult
        override suspend fun updateProfile(displayName: String?, bio: String?, avatarUrl: String?, coverUrl: String?) = Result.success(Unit)
        override suspend fun getAchievementCatalog() = Result.success(emptyList<AchievementDef>())
        override suspend fun getAchievements(userId: String) = Result.success(emptyList<AchievementView>())
        override suspend fun getOwnedTitles(userId: String) = Result.success(emptyList<OwnedTitle>())
        override suspend fun setActiveTitle(titleId: String?) = Result.success(Unit)
        override suspend fun checkinDaily(): Result<CheckinResult> = checkinResult
        override suspend fun getStoneHistory(userId: String, limit: Int) = Result.success(emptyList<SpiritStoneTxn>())
        override suspend fun spendStones(itemType: String, itemId: String, cost: Int) = Result.success(0L)
    }

    @Test
    fun checkIn_whenRemoteFails_doesNotLockCheckIn_andRecordsError() = runTest {
        val store = TestPreferenceStore()
        val uiPreferences = UiPreferences(store)
        val failingRepo = FakeGamificationRepository(
            checkinResult = Result.failure(Exception("RPC checkin_daily function does not exist"))
        )

        val viewModel = ProfileViewModel(
            remoteUseCases = null,
            badgeRepository = null,
            readingStatisticsRepository = null,
            gamificationRepository = failingRepo,
            uiPreferences = uiPreferences
        )

        viewModel.checkIn()
        advanceUntilIdle()

        // MUST NOT be locked out if the call failed
        assertFalse(viewModel.state.value.hasCheckedInToday, "Should not be marked checked-in when remote fails")
        assertEquals("", uiPreferences.lastCheckinDate().get(), "Should not store today's date in preferences on failure")
        assertNotNull(viewModel.state.value.checkinError, "Error should be recorded so user knows why check-in failed")
    }

    @Test
    fun checkIn_whenOfflineOrSignedOut_awardsLocalSpiritStones() = runTest {
        val store = TestPreferenceStore()
        val uiPreferences = UiPreferences(store)

        val viewModel = ProfileViewModel(
            remoteUseCases = null,
            badgeRepository = null,
            readingStatisticsRepository = null,
            gamificationRepository = null,
            uiPreferences = uiPreferences
        )

        viewModel.checkIn()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.hasCheckedInToday, "Local check-in should succeed")
        assertTrue(viewModel.state.value.spiritStones >= 10, "Local check-in should award spirit stones")
        assertEquals(viewModel.state.value.spiritStones, uiPreferences.localSpiritStones().get(), "Local stones preference should match state")
        val todayStr = currentTimeToLong().formatIsoDate()
        assertEquals(todayStr, uiPreferences.lastCheckinDate().get())
    }

    @Test
    fun checkIn_whenRemoteSucceeds_updatesStateAndLocalPreference() = runTest {
        val store = TestPreferenceStore()
        val uiPreferences = UiPreferences(store)
        val successRepo = FakeGamificationRepository(
            checkinResult = Result.success(CheckinResult(already = false, streakDay = 5, reward = 10)),
            profileResult = Result.success(GamificationProfile(userId = "u1", spiritStones = 75, checkinStreak = 5))
        )

        val viewModel = ProfileViewModel(
            remoteUseCases = null,
            badgeRepository = null,
            readingStatisticsRepository = null,
            gamificationRepository = successRepo,
            uiPreferences = uiPreferences
        )

        viewModel.checkIn()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.hasCheckedInToday)
        assertEquals(5, viewModel.state.value.checkinStreak)
        assertEquals(10, viewModel.state.value.lastCheckinReward)
        val todayStr = currentTimeToLong().formatIsoDate()
        assertEquals(todayStr, uiPreferences.lastCheckinDate().get())
    }
}
