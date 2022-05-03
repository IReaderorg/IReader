package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences

class FontHeightUseCase(
    private val appPreferences: AppPreferences,
) {
    fun save(fontHeight: Int) {
        appPreferences.lineHeight().set(fontHeight)
    }

    suspend  fun read(): Int {
        return appPreferences.lineHeight().get()
    }
}
