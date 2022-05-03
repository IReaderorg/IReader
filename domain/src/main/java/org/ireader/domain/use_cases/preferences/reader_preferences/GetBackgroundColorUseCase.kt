package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences

class BackgroundColorUseCase(
    private val appPreferences: AppPreferences,
) {
    fun save(value: Int) {
        appPreferences.backgroundColorReader().set(value)
    }

    suspend fun read(): Int {
        return appPreferences.backgroundColorReader().get()
    }
}

class TextColorUseCase(
    private val appPreferences: AppPreferences,
) {
    fun save(value: Int) {
        appPreferences.textColorReader().set(value)
    }

    suspend  fun read(): Int {
        return appPreferences.textColorReader().get()
    }
}
