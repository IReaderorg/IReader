package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import javax.inject.Inject

class FontSizeStateUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun save(fontSize: Int) {
        appPreferences.fontSize().set(fontSize)
    }

    fun read(): Int {
        return appPreferences.fontSize().get()
    }

}
