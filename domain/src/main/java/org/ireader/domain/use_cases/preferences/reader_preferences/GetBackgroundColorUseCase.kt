package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import javax.inject.Inject

class BackgroundColorUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun save(index: Int) {
        appPreferences.backgroundColorIndex().set(index)
    }

    fun read(): Int {
        return appPreferences.backgroundColorIndex().get()
    }
}
