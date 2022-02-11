package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.domain.ui.AppPreferences

class ReadFontSizeStateUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Int {
        return appPreferences.fontSize().get()
    }
}

class SaveFontSizeStateUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(fontSize: Int) {
        appPreferences.fontSize().set(fontSize)
    }
}
