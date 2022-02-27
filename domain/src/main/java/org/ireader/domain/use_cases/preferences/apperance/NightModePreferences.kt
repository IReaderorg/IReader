package org.ireader.domain.use_cases.preferences.apperance

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.core_ui.theme.ThemeMode
import org.ireader.core_ui.theme.UiPreferences


class SaveNightModePreferences(
    private val uiPreferences: UiPreferences,
) {
    operator fun invoke(mode: ThemeMode) {
        uiPreferences.themeMode().set(mode)
    }
}

class ReadNightModePreferences(
    private val uiPreferences: UiPreferences,
) {
    operator fun invoke(): Flow<ThemeMode> = flow {
        emit(uiPreferences.themeMode().get())
    }
}
