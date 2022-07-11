package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.preferences.ReaderPreferences

class FontHeightUseCase(
    private val prefs: ReaderPreferences,
) {
    fun save(fontHeight: Int) {
        prefs.lineHeight().set(fontHeight)
    }

    suspend fun read(): Int {
        return prefs.lineHeight().get()
    }
}
