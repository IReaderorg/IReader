package org.ireader.infinity.core.domain.use_cases.preferences.apperance

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.domain.ui.AppPreferences


class SaveNightModePreferences(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(mode: NightMode) {
        //  appPreferences.themeMode().set(mode.mode)
    }
}

class ReadNightModePreferences(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Flow<NightMode> = flow {
//        when (appPreferences.themeMode().get()) {
//            NightMode.FollowSystem.mode -> emit(NightMode.FollowSystem)
//            NightMode.Enable.mode -> emit(NightMode.Enable)
//            NightMode.Disable.mode -> emit(NightMode.Disable)
//            else -> emit(NightMode.Disable)
//        }
    }
}

sealed class NightMode(val mode: Int) {
    object FollowSystem : NightMode(0)
    object Enable : NightMode(1)
    object Disable : NightMode(2)
}
