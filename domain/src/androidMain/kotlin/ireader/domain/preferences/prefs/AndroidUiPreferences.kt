package ireader.domain.preferences.prefs

import android.content.pm.ActivityInfo
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.googlefonts.GoogleFont
import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.models.FontType
import ireader.domain.preferences.models.ReaderColors
import ireader.domain.preferences.models.getDefaultFont
import ireader.domain.preferences.models.prefs.asColor
import ireader.domain.preferences.models.prefs.asFont
import ireader.domain.preferences.models.prefs.asReaderTheme
import ireader.domain.preferences.models.prefs.asVoice

class AndroidUiPreferences @OptIn(ExperimentalTextApi::class) constructor(
    private val preferenceStore: PreferenceStore,
    private val provider: GoogleFont.Provider
) {

    companion object PreferenceKeys {
        const val SAVED_FONT_PREFERENCES = "reader_font_family"
        const val SAVED_BACKGROUND_COLOR = "background_color"
    }
    @OptIn(ExperimentalTextApi::class)
    fun font(): Preference<FontType> {
        return preferenceStore.getString(SAVED_FONT_PREFERENCES, getDefaultFont().name).asFont(provider)
    }

    fun backgroundColorReader(): Preference<Color> {
        return preferenceStore.getInt(SAVED_BACKGROUND_COLOR, Color(0xff262626).toArgb()).asColor()
    }
    fun backgroundColorTTS(): Preference<ReaderColors> {
        return preferenceStore.getLong("background_color_tts").asReaderTheme()
    }
    fun textColorReader(): Preference<Color> {
        return preferenceStore.getInt(ReaderPreferences.SAVED_TEXT_COLOR, Color(0xFFE9E9E9).toArgb()).asColor()
    }
    fun readerTheme(): Preference<ReaderColors> {
        return preferenceStore.getLong("readerTheme", 0).asReaderTheme()
    }
    fun speechVoice(): Preference<ireader.domain.preferences.models.prefs.IReaderVoice> {
        return preferenceStore.getString(ReaderPreferences.TEXT_READER_SPEECH_VOICE, "").asVoice()
    }
    fun unselectedScrollBarColor(): Preference<Color> {
        return preferenceStore.getInt(ReaderPreferences.SCROLL_INDICATOR_SELECTED_COLOR, Color(0xFF2A59B6).toArgb())
            .asColor()
    }
    fun selectedScrollBarColor(): Preference<Color> {
        return preferenceStore.getInt(ReaderPreferences.SCROLL_INDICATOR_UNSELECTED_COLOR, Color(0xFF5281CA).toArgb())
            .asColor()
    }
    fun orientation(): Preference<Int> {
        return preferenceStore.getInt(
            ReaderPreferences.SAVED_ORIENTATION,
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        )
    }
}