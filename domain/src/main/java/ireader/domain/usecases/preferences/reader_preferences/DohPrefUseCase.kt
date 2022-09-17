package ireader.domain.usecases.preferences.reader_preferences

import ireader.domain.preferences.prefs.AppPreferences
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
