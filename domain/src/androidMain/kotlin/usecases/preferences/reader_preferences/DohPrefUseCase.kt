package ireader.domain.usecases.preferences.reader_preferences

import ireader.domain.preferences.prefs.AppPreferences



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
