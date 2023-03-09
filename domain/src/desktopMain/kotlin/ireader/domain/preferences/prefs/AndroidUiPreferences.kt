package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.models.FontType
import ireader.domain.preferences.models.getDefaultFont

class DesktopUiPreferences constructor(
        private val preferenceStore: PreferenceStore,
) : PlatformUiPreferences {

    companion object PreferenceKeys {
        const val SAVED_FONT_PREFERENCES = "reader_font_family"

        
    }

    override fun font(): Preference<FontType> {
        return throw Exception()
    }
}