package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.preferences.ReaderPreferences
import org.ireader.core_ui.theme.FontType
import org.ireader.core_ui.theme.fonts

class SelectedFontStateUseCase(
    private val prefs: ReaderPreferences,
) {
    fun saveFont(fontIndex: Int) {
        prefs.font().set(fontIndex)
    }

    /**
     * fontIndex is the index of font which is in fonts list inside the Type package
     */
    fun readFont(): FontType {
        val fontType = prefs.font().get()
        return fonts.getOrNull(fontType)?: FontType.Roboto
    }

    fun saveSelectableText(value: Boolean) {
        prefs.selectableText().set(value)
    }

    suspend fun readSelectableText(): Boolean {
        return prefs.selectableText().get()
    }
}
