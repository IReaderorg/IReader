package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import javax.inject.Inject

class ReadDohPrefUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Int {
        return appPreferences.dohStateKey().get()
    }
}

class SaveDohPrefUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(dohPref: Int) {
        appPreferences.dohStateKey().set(dohPref)
    }
}