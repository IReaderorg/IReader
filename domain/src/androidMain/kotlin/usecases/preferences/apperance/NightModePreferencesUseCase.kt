package ireader.domain.usecases.preferences.apperance

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
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
