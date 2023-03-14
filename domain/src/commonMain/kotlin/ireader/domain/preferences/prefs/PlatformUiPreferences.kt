package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.domain.preferences.models.FontType


interface PlatformUiPreferences {
    fun font(): Preference<FontType>?
}