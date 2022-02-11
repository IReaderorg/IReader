package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.domain.ui.AppPreferences

class ReadDohPrefUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Int {
        return appPreferences.dohStateKey().get()
    }
}

class SaveDohPrefUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(dohPref: Int) {
        appPreferences.dohStateKey().set(dohPref)
    }
}