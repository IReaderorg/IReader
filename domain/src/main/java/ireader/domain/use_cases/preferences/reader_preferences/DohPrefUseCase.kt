package ireader.domain.use_cases.preferences.reader_preferences

import ireader.core.ui.preferences.AppPreferences
import org.koin.core.annotation.Factory

@Factory
class DohPrefUseCase(
    private val appPreferences: AppPreferences,
) {
    fun save(dohPref: Int) {
        appPreferences.dohStateKey().set(dohPref)
    }

    fun read(): Int {
        return appPreferences.dohStateKey().get()
    }
}
