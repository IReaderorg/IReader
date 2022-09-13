package ireader.domain.usecases.preferences.apperance

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ireader.core.ui.preferences.PreferenceValues
import ireader.core.ui.preferences.UiPreferences
import org.koin.core.annotation.Factory

@Factory
class NightModePreferencesUseCase(
    private val uiPreferences: UiPreferences,
) {
    fun save(mode: PreferenceValues.ThemeMode) {
        uiPreferences.themeMode().set(mode)
    }

    suspend fun read(): Flow<PreferenceValues.ThemeMode> = flow {
        emit(uiPreferences.themeMode().get())
    }
}
