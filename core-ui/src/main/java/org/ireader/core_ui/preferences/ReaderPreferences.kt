package org.ireader.core_ui.preferences

import android.content.pm.ActivityInfo
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.googlefonts.GoogleFont
import org.ireader.core_api.prefs.Preference
import org.ireader.core_api.prefs.PreferenceStore
import org.ireader.core_api.prefs.getEnum
import org.ireader.core_ui.theme.FontType
import org.ireader.core_ui.theme.Roboto
import org.ireader.core_ui.theme.prefs.IReaderVoice
import org.ireader.core_ui.theme.prefs.asColor
import org.ireader.core_ui.theme.prefs.asFont
import org.ireader.core_ui.theme.prefs.asVoice
import org.ireader.core_ui.ui.PreferenceAlignment

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
        const val READING_MODE = "reader_mode"

        const val SAVED_BACKGROUND_COLOR = "background_color"
        const val SAVED_TEXT_COLOR = "text_color"
        const val SAVED_FONT_HEIGHT = "font_height"
        const val SAVED_PARAGRAPH_DISTANCE = "paragraph_distance"
        const val SAVED_PARAGRAPH_INDENT = "paragraph_indent"
        const val SAVED_ORIENTATION = "orientation_reader_screen"
        const val SLEEP_TIMER = "tts_sleep_timer"
        const val SLEEP_TIMER_MODE = "tts_sleep_mode"

        const val SCROLL_MODE = "scroll_mode"
        const val AUTO_SCROLL_MODE_INTERVAL = "auto_scroll_mode_interval"
        const val AUTO_SCROLL_MODE_OFFSET = "auto_scroll_mode_offset"
        const val SCROLL_INDICATOR_PADDING = "scroll_indicator_padding"
        const val SCROLL_INDICATOR_WIDTH = "scroll_indicator_width"
        const val SCROLL_INDICATOR_IS_ENABLE = "scroll_indicator_is_enable"
        const val SCROLL_INDICATOR_IS_DRAGGABLE = "scroll_indicator_is_draggable"
        const val SCROLL_INDICATOR_SELECTED_COLOR = "scroll_indicator_selected_color"
        const val SCROLL_INDICATOR_UNSELECTED_COLOR = "scroll_indicator_unselected_color"
        const val SCROLL_INDICATOR_ALIGNMENT = "scroll_indicator_alignment"
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
        return preferenceStore.getInt(SAVED_BACKGROUND_COLOR, Color(0xff262626).toArgb()).asColor()
    }

    fun textColorReader(): Preference<Color> {
        return preferenceStore.getInt(SAVED_TEXT_COLOR, Color(0xFFE9E9E9).toArgb()).asColor()
    }

    fun webViewIntegration(): Preference<Boolean> {
        return preferenceStore.getBoolean("webView_integration", false)
    }

    fun unselectedScrollBarColor(): Preference<Color> {
        return preferenceStore.getInt(SCROLL_INDICATOR_SELECTED_COLOR, Color(0xFF2A59B6).toArgb())
            .asColor()
    }

    fun selectedScrollBarColor(): Preference<Color> {
        return preferenceStore.getInt(SCROLL_INDICATOR_UNSELECTED_COLOR, Color(0xFF5281CA).toArgb())
            .asColor()
    }

    fun scrollBarAlignment(): Preference<PreferenceAlignment> {
        return preferenceStore.getEnum(SCROLL_INDICATOR_ALIGNMENT, PreferenceAlignment.Right)
    }

    fun lineHeight(): Preference<Int> {
        return preferenceStore.getInt(SAVED_FONT_HEIGHT, 25)
    }

    fun readingMode(): Preference<ReadingMode> {
        return preferenceStore.getEnum(READING_MODE, ReadingMode.Page)
    }

    fun paragraphDistance(): Preference<Int> {
        return preferenceStore.getInt(SAVED_PARAGRAPH_DISTANCE, 2)
    }

    fun orientation(): Preference<Int> {
        return preferenceStore.getInt(
            SAVED_ORIENTATION,
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        )
    }

    fun sleepTime(): Preference<Long> {
        return preferenceStore.getLong(SLEEP_TIMER, 15)
    }

    fun sleepMode(): Preference<Boolean> {
        return preferenceStore.getBoolean(SLEEP_TIMER_MODE, false)
    }

    fun textAlign(): Preference<org.ireader.core_ui.ui.PreferenceAlignment> {
        return preferenceStore.getEnum(
            TEXT_ALIGNMENT,
            org.ireader.core_ui.ui.PreferenceAlignment.Left
        )
    }

    fun paragraphIndent(): Preference<Int> {
        return preferenceStore.getInt(SAVED_PARAGRAPH_INDENT, 8)
    }

    fun topMargin(): Preference<Int> {
        return preferenceStore.getInt("reader_top_margin", 0)
    }

    fun leftMargin(): Preference<Int> {
        return preferenceStore.getInt("reader_left_margin", 0)
    }

    fun rightMargin(): Preference<Int> {
        return preferenceStore.getInt("reader_right_margin", 0)
    }

    fun bottomMargin(): Preference<Int> {
        return preferenceStore.getInt("reader_bottom_margin", 0)
    }

    fun topContentPadding(): Preference<Int> {
        return preferenceStore.getInt("reader_top_padding", 2)
    }

    fun bottomContentPadding(): Preference<Int> {
        return preferenceStore.getInt("reader_bottom_padding", 2)
    }

    fun betweenLetterSpaces(): Preference<Int> {
        return preferenceStore.getInt("reader_text_space", 0)
    }

    fun textWeight(): Preference<Int> {
        return preferenceStore.getInt("reader_text_weight", 400)
    }

    fun screenAlwaysOn(): Preference<Boolean> {
        return preferenceStore.getBoolean("reader_always_on", false)
    }

    fun scrollMode(): Preference<Boolean> {
        return preferenceStore.getBoolean(SCROLL_MODE, true)
    }

    fun showScrollIndicator(): Preference<Boolean> {
        return preferenceStore.getBoolean(SCROLL_INDICATOR_IS_ENABLE, true)
    }

    fun scrollbarMode(): Preference<PreferenceValues.ScrollbarSelectionMode> {
        return preferenceStore.getEnum(
            SCROLL_INDICATOR_IS_DRAGGABLE,
            PreferenceValues.ScrollbarSelectionMode.Full
        )
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

    fun showChapterNumberPreferences(): Preference<ChapterDisplayMode> {
        return preferenceStore.getEnum("chapter_layout_mode", ChapterDisplayMode.Default)
    }
}

enum class ReadingMode {
    Page,
    Continues;

    companion object {
        fun valueOf(index: Int): ReadingMode {
            return when (index) {
                Page.ordinal -> Page
                Continues.ordinal -> Continues
                else -> throw IllegalArgumentException()
            }
        }
    }
}

enum class ChapterDisplayMode {
    Default,
    SourceTitle,
    ChapterNumber
}
