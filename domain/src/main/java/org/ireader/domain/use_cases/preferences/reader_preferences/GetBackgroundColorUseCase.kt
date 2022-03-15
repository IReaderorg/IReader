package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import javax.inject.Inject

class BackgroundColorUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun save(value: Int) {
        appPreferences.backgroundColorReader().set(value)
    }

    fun read(): Int {
        return appPreferences.backgroundColorReader().get()
    }
}

class TextColorUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun save(value: Int) {
        appPreferences.textColorReader().set(value)
    }

    fun read(): Int {
        return appPreferences.textColorReader().get()
    }
}