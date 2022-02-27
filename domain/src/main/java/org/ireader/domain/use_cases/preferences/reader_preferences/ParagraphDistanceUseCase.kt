package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences


class SaveParagraphDistanceUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(paragraphDistance: Int) {
        appPreferences.paragraphDistance().set(paragraphDistance)
    }
}

class ReadParagraphDistanceUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Int {
        return appPreferences.paragraphDistance().get()
    }
}

class SaveParagraphIndentUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(paragraphIndent: Int) {
        appPreferences.paragraphIndent().set(paragraphIndent)
    }
}

class ReadParagraphIndentUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Int {
        return appPreferences.paragraphIndent().get()
    }
}
