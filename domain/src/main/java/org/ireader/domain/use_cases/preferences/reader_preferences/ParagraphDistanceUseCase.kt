package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import javax.inject.Inject


class SaveParagraphDistanceUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(paragraphDistance: Int) {
        appPreferences.paragraphDistance().set(paragraphDistance)
    }
}

class ReadParagraphDistanceUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Int {
        return appPreferences.paragraphDistance().get()
    }
}

class SaveParagraphIndentUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(paragraphIndent: Int) {
        appPreferences.paragraphIndent().set(paragraphIndent)
    }
}

class ReadParagraphIndentUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Int {
        return appPreferences.paragraphIndent().get()
    }
}
