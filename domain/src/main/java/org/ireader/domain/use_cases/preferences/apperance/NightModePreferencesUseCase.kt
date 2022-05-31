package org.ireader.domain.use_cases.preferences.apperance

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.core_ui.preferences.PreferenceValues
import org.ireader.core_ui.preferences.UiPreferences
import javax.inject.Inject

class NightModePreferencesUseCase @Inject constructor(
    private val uiPreferences: UiPreferences,
) {
    fun save(mode: PreferenceValues.ThemeMode) {
        uiPreferences.themeMode().set(mode)
    }

    suspend fun read(): Flow<PreferenceValues.ThemeMode> = flow {
        emit(uiPreferences.themeMode().get())
    }
}
