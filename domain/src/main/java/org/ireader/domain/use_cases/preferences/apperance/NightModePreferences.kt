package org.ireader.infinity.core.domain.use_cases.preferences.apperance

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.domain.repository.Repository


class SaveNightModePreferences(
    private val repository: Repository,
) {
    operator fun invoke(mode: NightMode) {
        repository.preferencesHelper.nightModeKey.set(mode.mode)
    }
}

class ReadNightModePreferences(
    private val repository: Repository,
) {
    operator fun invoke(): Flow<NightMode> = flow {
        when (repository.preferencesHelper.nightModeKey.get()) {
            NightMode.FollowSystem.mode -> emit(NightMode.FollowSystem)
            NightMode.Enable.mode -> emit(NightMode.Enable)
            NightMode.Disable.mode -> emit(NightMode.Disable)
            else -> emit(NightMode.Disable)
        }
    }
}

sealed class NightMode(val mode: Int) {
    object FollowSystem : NightMode(0)
    object Enable : NightMode(1)
    object Disable : NightMode(2)
}
