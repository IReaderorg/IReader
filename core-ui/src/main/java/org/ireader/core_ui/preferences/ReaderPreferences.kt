package org.ireader.core_ui.preferences

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.googlefonts.GoogleFont
import org.ireader.core_api.prefs.Preference
import org.ireader.core_api.prefs.PreferenceStore
import org.ireader.core_api.prefs.getEnum
import org.ireader.core_ui.theme.FontType
import org.ireader.core_ui.theme.OrientationMode
import org.ireader.core_ui.theme.Roboto
import org.ireader.core_ui.theme.prefs.IReaderVoice
import org.ireader.core_ui.theme.prefs.asColor
import org.ireader.core_ui.theme.prefs.asFont
import org.ireader.core_ui.theme.prefs.asVoice

class ReaderPreferences @OptIn(ExperimentalTextApi::class) constructor(
    private val preferenceStore: PreferenceStore,
    private val provider: GoogleFont.Provider
) {
    companion object PreferenceKeys {
        const val SAVED_FONT_SIZE_PREFERENCES = "reader_font_size"
        const val SAVED_FONT_PREFERENCES = "reader_font_family"
        const val SAVED_BRIGHTNESS_PREFERENCES = "reader_brightness"
        const val SAVED_IMMERSIVE_MODE_PREFERENCES = "reader_immersive_mode"
        const val SAVED_AUTO_BRIGHTNESS_PREFERENCES = "reader_auto_brightness"

        const val SAVED_BACKGROUND_COLOR = "background_color"
        const val SAVED_TEXT_COLOR = "text_color"
        const val SAVED_FONT_HEIGHT = "font_height"
        const val SAVED_PARAGRAPH_DISTANCE = "paragraph_distance"
        const val SAVED_PARAGRAPH_INDENT = "paragraph_indent"
        const val SAVED_ORIENTATION = "orientation_reader"

        const val SCROLL_MODE = "scroll_mode"
        const val AUTO_SCROLL_MODE_INTERVAL = "auto_scroll_mode_interval"
        const val AUTO_SCROLL_MODE_OFFSET = "auto_scroll_mode_offset"
        const val SCROLL_INDICATOR_PADDING = "scroll_indicator_padding"
        const val SCROLL_INDICATOR_WIDTH = "scroll_indicator_width"
        const val SCROLL_INDICATOR_IS_ENABLE = "scroll_indicator_is_enable"
        const val SCROLL_INDICATOR_IS_DRAGGABLE = "scroll_indicator_is_draggable"
        const val SELECTABLE_TEXT = "selectable_text"
        const val TEXT_ALIGNMENT = "text_alignment"



        const val TEXT_READER_SPEECH_RATE = "text_reader_speech_rate"
        const val TEXT_READER_SPEECH_PITCH = "text_reader_speech_pitch"
        const val TEXT_READER_SPEECH_LANGUAGE = "text_reader_speech_language"
        const val TEXT_READER_SPEECH_VOICE = "text_reader_speech_selected_voice"
        const val TEXT_READER_AUTO_NEXT = "text_reader_auto_next"
    }

    fun brightness(): Preference<Float> {
        return preferenceStore.getFloat(SAVED_BRIGHTNESS_PREFERENCES, .5F)
    }

    fun immersiveMode(): Preference<Boolean> {
        return preferenceStore.getBoolean(SAVED_IMMERSIVE_MODE_PREFERENCES, false)
    }

    fun autoBrightness(): Preference<Boolean> {
        return preferenceStore.getBoolean(SAVED_AUTO_BRIGHTNESS_PREFERENCES, true)
    }

    fun fontSize(): Preference<Int> {
        return preferenceStore.getInt(SAVED_FONT_SIZE_PREFERENCES, 18)
    }

    @OptIn(ExperimentalTextApi::class)
    fun font(): Preference<FontType> {
        return preferenceStore.getString(SAVED_FONT_PREFERENCES, Roboto.fontName).asFont(provider)
    }
    fun backgroundColorReader(): Preference<Color> {
        return preferenceStore.getInt(SAVED_BACKGROUND_COLOR, Color.Black.toArgb()).asColor()
    }

    fun textColorReader(): Preference<Color> {
        return preferenceStore.getInt(SAVED_TEXT_COLOR, Color.White.toArgb()).asColor()
    }

    fun lineHeight(): Preference<Int> {
        return preferenceStore.getInt(SAVED_FONT_HEIGHT, 25)
    }

    fun paragraphDistance(): Preference<Int> {
        return preferenceStore.getInt(SAVED_PARAGRAPH_DISTANCE, 2)
    }

    fun orientation(): Preference<OrientationMode> {
        return preferenceStore.getEnum(SAVED_ORIENTATION, OrientationMode.Portrait)
    }

    fun textAlign(): Preference<org.ireader.core_ui.ui.TextAlign> {
        return preferenceStore.getEnum(TEXT_ALIGNMENT, org.ireader.core_ui.ui.TextAlign.Left)
    }

    fun paragraphIndent(): Preference<Int> {
        return preferenceStore.getInt(SAVED_PARAGRAPH_INDENT, 8)
    }

    fun scrollMode(): Preference<Boolean> {
        return preferenceStore.getBoolean(SCROLL_MODE, true)
    }

    fun showScrollIndicator(): Preference<Boolean> {
        return preferenceStore.getBoolean(SCROLL_INDICATOR_IS_ENABLE, true)
    }
    fun isScrollIndicatorDraggable(): Preference<Boolean> {
        return preferenceStore.getBoolean(SCROLL_INDICATOR_IS_DRAGGABLE, true)
    }

    fun selectableText(): Preference<Boolean> {
        return preferenceStore.getBoolean(SELECTABLE_TEXT, false)
    }

    fun autoScrollInterval(): Preference<Long> {
        return preferenceStore.getLong(AUTO_SCROLL_MODE_INTERVAL, 5000L)
    }

    fun autoScrollOffset(): Preference<Int> {
        return preferenceStore.getInt(AUTO_SCROLL_MODE_OFFSET, 500)
    }

    fun scrollIndicatorWith(): Preference<Int> {
        return preferenceStore.getInt(SCROLL_INDICATOR_WIDTH, 2)
    }

    fun scrollIndicatorPadding(): Preference<Int> {
        return preferenceStore.getInt(SCROLL_INDICATOR_PADDING, 4)
    }

    fun speechRate(): Preference<Float> {
        return preferenceStore.getFloat(TEXT_READER_SPEECH_RATE, .8f)
    }

    fun readerAutoNext(): Preference<Boolean> {
        return preferenceStore.getBoolean(TEXT_READER_AUTO_NEXT, false)
    }

    fun speechPitch(): Preference<Float> {
        return preferenceStore.getFloat(TEXT_READER_SPEECH_PITCH, .8f)
    }

    fun speechVoice(): Preference<IReaderVoice> {
        return preferenceStore.getString(TEXT_READER_SPEECH_VOICE, "").asVoice()
    }

    fun speechLanguage(): Preference<String> {
        return preferenceStore.getString(TEXT_READER_SPEECH_LANGUAGE, "")
    }

}