package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import javax.inject.Inject

class SaveFontHeightUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(fontHeight: Int) {
        appPreferences.lineHeight().set(fontHeight)
    }
}

class ReadFontHeightUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Int {
        return appPreferences.lineHeight().get()
    }
}
