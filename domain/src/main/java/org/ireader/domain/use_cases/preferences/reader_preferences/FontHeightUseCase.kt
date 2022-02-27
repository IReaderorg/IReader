package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences

class SaveFontHeightUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(fontHeight: Int) {
        appPreferences.lineHeight().set(fontHeight)
    }
}

class ReadFontHeightUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Int {
        return appPreferences.lineHeight().get()
    }
}
