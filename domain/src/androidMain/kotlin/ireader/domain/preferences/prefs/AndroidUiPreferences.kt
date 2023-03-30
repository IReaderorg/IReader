package ireader.domain.preferences.prefs

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.googlefonts.GoogleFont
import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.models.FontType
import ireader.domain.preferences.models.getDefaultFont
import ireader.domain.preferences.models.prefs.asFont

class AndroidUiPreferences @OptIn(ExperimentalTextApi::class) constructor(
        private val preferenceStore: PreferenceStore,
        private val provider: GoogleFont.Provider
) : PlatformUiPreferences {

    companion object PreferenceKeys {
        const val SAVED_FONT_PREFERENCES = "reader_font_family"


    }
    @OptIn(ExperimentalTextApi::class)
    override fun font(): Preference<FontType> {
        return preferenceStore.getString(SAVED_FONT_PREFERENCES, getDefaultFont().name).asFont(provider)
    }
}