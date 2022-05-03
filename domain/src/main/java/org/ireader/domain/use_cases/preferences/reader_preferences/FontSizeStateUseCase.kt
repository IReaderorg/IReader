package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences

class FontSizeStateUseCase(
    private val appPreferences: AppPreferences,
) {
    fun save(fontSize: Int) {
        appPreferences.fontSize().set(fontSize)
    }

    suspend fun read(): Int {
        return appPreferences.fontSize().get()
    }
}
