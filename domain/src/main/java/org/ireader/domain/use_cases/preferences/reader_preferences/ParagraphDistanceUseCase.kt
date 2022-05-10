package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.preferences.ReaderPreferences

class ParagraphDistanceUseCase(
    private val prefs: ReaderPreferences,
) {
    fun save(paragraphDistance: Int) {
        prefs.paragraphDistance().set(paragraphDistance)
    }

    suspend fun read(): Int {
        return prefs.paragraphDistance().get()
    }
}

class ParagraphIndentUseCase(
    private val prefs: ReaderPreferences,
) {
    fun save(paragraphIndent: Int) {
        prefs.paragraphIndent().set(paragraphIndent)
    }

    suspend fun read(): Int {
        return prefs.paragraphIndent().get()
    }
}
