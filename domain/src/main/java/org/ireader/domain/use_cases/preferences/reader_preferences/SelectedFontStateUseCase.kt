package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import org.ireader.core_ui.theme.FontType
import org.ireader.core_ui.theme.fonts
import javax.inject.Inject

class SelectedFontStateUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun saveFont(fontIndex: Int) {
        appPreferences.font().set(fontIndex)
    }

    /**
     * fontIndex is the index of font which is in fonts list inside the Type package
     */
    fun readFont(): FontType {
        val fontType = appPreferences.font().get()
        return fonts[fontType]
    }

    fun saveSelectableText(value: Boolean) {
        appPreferences.selectableText().set(value)
    }

    fun readSelectableText(): Boolean {
        return appPreferences.selectableText().get()
    }


}
