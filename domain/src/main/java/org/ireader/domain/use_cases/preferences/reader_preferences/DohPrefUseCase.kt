package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import javax.inject.Inject

class DohPrefUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun save(dohPref: Int) {
        appPreferences.dohStateKey().set(dohPref)
    }

    fun read(): Int {
        return appPreferences.dohStateKey().get()
    }
}
