package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import javax.inject.Inject

class FontHeightUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun save(fontHeight: Int) {
        appPreferences.lineHeight().set(fontHeight)
    }

    fun read(): Int {
        return appPreferences.lineHeight().get()
    }


}