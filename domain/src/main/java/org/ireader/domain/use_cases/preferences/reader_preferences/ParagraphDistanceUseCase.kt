package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import javax.inject.Inject

class ParagraphDistanceUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun save(paragraphDistance: Int) {
        appPreferences.paragraphDistance().set(paragraphDistance)
    }

    suspend fun read(): Int {
        return appPreferences.paragraphDistance().get()
    }
}

class ParagraphIndentUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun save(paragraphIndent: Int) {
        appPreferences.paragraphIndent().set(paragraphIndent)
    }

    suspend fun read(): Int {
        return appPreferences.paragraphIndent().get()
    }
}
