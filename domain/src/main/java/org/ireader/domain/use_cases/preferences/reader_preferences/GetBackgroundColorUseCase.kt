package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.preferences.ReaderPreferences

class BackgroundColorUseCase(
    private val prefs: ReaderPreferences,
) {
    fun save(value: Int) {
        prefs.backgroundColorReader().set(value)
    }

    suspend fun read(): Int {
        return prefs.backgroundColorReader().get()
    }
}

class TextColorUseCase(
    private val prefs: ReaderPreferences,
) {
    fun save(value: Int) {
        prefs.textColorReader().set(value)
    }

    suspend  fun read(): Int {
        return prefs.textColorReader().get()
    }
}
