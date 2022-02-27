package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import org.ireader.core_ui.theme.FontType
import org.ireader.core_ui.theme.fonts

class ReadSelectedFontStateUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): FontType {
        val fontType = appPreferences.font().get()
        return fonts[fontType]
    }
}

class SaveSelectedFontStateUseCase(
    private val appPreferences: AppPreferences,
) {
    /**
     * fontIndex is the index of font which is in fonts list inside the Type package
     */
    operator fun invoke(fontIndex: Int) {
        appPreferences.font().set(fontIndex)
    }
}