package ireader.domain.preferences.prefs

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import ireader.core.prefs.Preference
import ireader.domain.preferences.models.FontType
import ireader.domain.preferences.models.ReaderColors
import ireader.domain.preferences.models.prefs.asColor
import ireader.domain.preferences.models.prefs.asReaderTheme
import ireader.domain.preferences.models.prefs.asVoice


interface PlatformUiPreferences {
    fun font(): Preference<FontType>
}