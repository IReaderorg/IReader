package ireader.domain.usecases.preferences.reader_preferences

import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.models.FontType
import ireader.domain.preferences.prefs.AndroidUiPreferences

class SelectedFontStateUseCase(
    private val prefs: ReaderPreferences,
    private val androidPrefs: AndroidUiPreferences,
    ) {
    fun saveFont(fontIndex: FontType) {
        androidPrefs.font().set(fontIndex)
    }

    /**
     * fontIndex is the index of font which is in fonts list inside the Type package
     */
    fun readFont(): FontType {
        return androidPrefs.font().get()
    }

    fun saveSelectableText(value: Boolean) {
        prefs.selectableText().set(value)
    }

    suspend fun readSelectableText(): Boolean {
        return prefs.selectableText().get()
    }
}
