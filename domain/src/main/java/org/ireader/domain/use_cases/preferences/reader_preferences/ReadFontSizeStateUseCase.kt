package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import javax.inject.Inject

class ReadFontSizeStateUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Int {
        return appPreferences.fontSize().get()
    }
}

class SaveFontSizeStateUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(fontSize: Int) {
        appPreferences.fontSize().set(fontSize)
    }
}
