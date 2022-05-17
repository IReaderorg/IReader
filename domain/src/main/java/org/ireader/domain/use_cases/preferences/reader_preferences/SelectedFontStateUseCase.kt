package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.preferences.ReaderPreferences
import org.ireader.core_ui.theme.FontType

class SelectedFontStateUseCase(
    private val prefs: ReaderPreferences,
) {
    fun saveFont(fontIndex: FontType) {
        prefs.font().set(fontIndex)
    }

    /**
     * fontIndex is the index of font which is in fonts list inside the Type package
     */
    fun readFont(): FontType {
        return prefs.font().get()
    }

    fun saveSelectableText(value: Boolean) {
        prefs.selectableText().set(value)
    }

    suspend fun readSelectableText(): Boolean {
        return prefs.selectableText().get()
    }
}
