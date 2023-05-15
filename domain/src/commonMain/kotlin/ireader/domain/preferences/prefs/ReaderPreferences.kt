package ireader.domain.preferences.prefs

import androidx.compose.ui.text.ExperimentalTextApi
import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.core.prefs.getEnum
import ireader.domain.models.prefs.PreferenceValues

@OptIn(ExperimentalTextApi::class)
class ReaderPreferences constructor(
    private val preferenceStore: PreferenceStore,
) {
    companion object PreferenceKeys {
        const val SAVED_FONT_SIZE_PREFERENCES = "reader_font_size"

        const val SAVED_BRIGHTNESS_PREFERENCES = "reader_brightness"
        const val SAVED_IMMERSIVE_MODE_PREFERENCES = "reader_immersive_mode"
        const val SAVED_AUTO_BRIGHTNESS_PREFERENCES = "reader_auto_brightness"
        const val READING_MODE = "reader_mode"


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



    fun followTTSSpeaker(): Preference<Boolean> {
        return preferenceStore.getBoolean("follow_tts_speaker",false)
    }



    fun webViewIntegration(): Preference<Boolean> {
        return preferenceStore.getBoolean("webView_integration", false)
    }





    fun scrollBarAlignment(): Preference<PreferenceValues.PreferenceTextAlignment> {
        return preferenceStore.getEnum(SCROLL_INDICATOR_ALIGNMENT, PreferenceValues.PreferenceTextAlignment.Right)
    }
    fun ttsIconAlignments(): Preference<PreferenceValues.PreferenceAlignment> {
        return preferenceStore.getEnum("tts_icons_alignments", PreferenceValues.PreferenceAlignment.TopLeft)
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


    fun sleepTime(): Preference<Long> {
        return preferenceStore.getLong(SLEEP_TIMER, 15)
    }

    fun sleepMode(): Preference<Boolean> {
        return preferenceStore.getBoolean(SLEEP_TIMER_MODE, false)
    }

    fun bionicReading(): Preference<Boolean> {
        return preferenceStore.getBoolean("ENABLE_BIONIC_READING", false)
    }

    fun textAlign(): Preference<PreferenceValues.PreferenceTextAlignment> {
        return preferenceStore.getEnum(
            TEXT_ALIGNMENT,
            PreferenceValues.PreferenceTextAlignment.Left
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



    fun speechLanguage(): Preference<String> {
        return preferenceStore.getString(TEXT_READER_SPEECH_LANGUAGE, "")
    }

    fun showChapterNumberPreferences(): Preference<ChapterDisplayMode> {
        return preferenceStore.getEnum("chapter_layout_mode", ChapterDisplayMode.Default)
    }
    fun translatorEngine(): Preference<Long> {
        return preferenceStore.getLong("translatorEngine", -1)
    }

    fun translatorOriginLanguage(): Preference<String> {
        return preferenceStore.getString("translator_origin_language", "en")
    }
    fun translatorTargetLanguage(): Preference<String> {
        return preferenceStore.getString("translator_target_language", "en")
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
