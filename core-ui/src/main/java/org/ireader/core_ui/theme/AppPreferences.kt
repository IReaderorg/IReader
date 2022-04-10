package org.ireader.core_ui.theme

import tachiyomi.core.prefs.Preference
import tachiyomi.core.prefs.PreferenceStore
import tachiyomi.core.prefs.getEnum
import javax.inject.Inject


class AppPreferences @Inject constructor(
    private val preferenceStore: PreferenceStore,
) {
    companion object PreferenceKeys {
        const val SAVED_FONT_SIZE_PREFERENCES = "reader_font_size"
        const val SAVED_FONT_PREFERENCES = "reader_font_family"
        const val SAVED_BRIGHTNESS_PREFERENCES = "reader_brightness"
        const val SAVED_IMMERSIVE_MODE_PREFERENCES = "reader_immersive_mode"
        const val SAVED_AUTO_BRIGHTNESS_PREFERENCES = "reader_auto_brightness"

        const val SAVED_LIBRARY_LAYOUT_KEY = "library_layout_type"
        const val SAVED_BROWSE_LAYOUT_KEY = "browser_layout_type"
        const val SAVED_BACkGROUND_COLOR = "background_color"
        const val SAVED_TEXT_COLOR = "text_color"
        const val SAVED_FONT_HEIGHT = "font_height"
        const val SAVED_PARAGRAPH_DISTANCE = "paragraph_distance"
        const val SAVED_PARAGRAPH_INDENT = "paragraph_indent"
        const val SAVED_ORIENTATION = "orientation_reader"
        const val SORT_LIBRARY_SCREEN = "sort_library_screen"
        const val SORT_DESC_LIBRARY_SCREEN = "sort_desc_library_screen"
        const val SCROLL_MODE = "scroll_mode"
        const val AUTO_SCROLL_MODE_INTERVAL = "auto_scroll_mode_interval"
        const val AUTO_SCROLL_MODE_OFFSET = "auto_scroll_mode_offset"
        const val SCROLL_INDICATOR_PADDING = "scroll_indicator_padding"
        const val SCROLL_INDICATOR_WIDTH = "scroll_indicator_width"
        const val SELECTABLE_TEXT = "selectable_text"


        const val TEXT_READER_SPEECH_RATE = "text_reader_speech_rate"
        const val TEXT_READER_SPEECH_PITCH = "text_reader_speech_pitch"
        const val TEXT_READER_SPEECH_LANGUAGE = "text_reader_speech_language"
        const val TEXT_READER_SPEECH_VOICE = "text_reader_speech_voice"
        const val TEXT_READER_AUTO_NEXT = "text_reader_auto_next"

        /** Services **/
        const val Last_UPDATE_CHECK = "last_update_check"

        /** Setting Pref**/
        const val SAVED_DOH_KEY = "SAVED_DOH_KEY"
        const val THEME_MODE_KEY = "theme_mode_key"
        const val LIGHT_MODE_KEY = "theme_light"
        const val NIGHT_MODE_KEY = "theme_dark"
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

    fun font(): Preference<Int> {
        return preferenceStore.getInt(SAVED_FONT_PREFERENCES, 0)
    }

    fun libraryLayoutType(): Preference<Int> {
        return preferenceStore.getInt(SAVED_LIBRARY_LAYOUT_KEY, 0)
    }

    fun exploreLayoutType(): Preference<Int> {
        return preferenceStore.getInt(SAVED_BROWSE_LAYOUT_KEY, 0)
    }

    fun dohStateKey(): Preference<Int> {
        return preferenceStore.getInt(SAVED_DOH_KEY, 0)
    }

    fun backgroundColorReader(): Preference<Int> {
        return preferenceStore.getInt(SAVED_BACkGROUND_COLOR, -14277082)
    }

    fun textColorReader(): Preference<Int> {
        return preferenceStore.getInt(SAVED_TEXT_COLOR, -1447447)
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

    fun paragraphIndent(): Preference<Int> {
        return preferenceStore.getInt(SAVED_PARAGRAPH_INDENT, 8)
    }

    fun scrollMode(): Preference<Boolean> {
        return preferenceStore.getBoolean(SCROLL_MODE, true)
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

    fun sortLibraryScreen(): Preference<Int> {
        return preferenceStore.getInt(SORT_LIBRARY_SCREEN, 0)
    }

    fun sortDescLibraryScreen(): Preference<Boolean> {
        return preferenceStore.getBoolean(SORT_DESC_LIBRARY_SCREEN, true)
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

    fun speechVoice(): Preference<String> {
        return preferenceStore.getString(TEXT_READER_SPEECH_VOICE, "")
    }

    fun speechLanguage(): Preference<String> {
        return preferenceStore.getString(TEXT_READER_SPEECH_LANGUAGE, "")
    }

    fun lastUpdateCheck(): Preference<Long> {
        return preferenceStore.getLong(Last_UPDATE_CHECK, 0)
    }

}


class UiPreferences @Inject constructor(private val preferenceStore: PreferenceStore) {

    fun themeMode(): Preference<ThemeMode> {
        return preferenceStore.getEnum("theme_mode", ThemeMode.System)
    }

    fun lightTheme(): Preference<Int> {
        return preferenceStore.getInt("theme_light", 6)
    }

    fun darkTheme(): Preference<Int> {
        return preferenceStore.getInt("theme_dark", 5)
    }

    fun colorPrimaryLight(): Preference<Int> {
        return preferenceStore.getInt("color_primary_light", 0)
    }

    fun colorPrimaryDark(): Preference<Int> {
        return preferenceStore.getInt("color_primary_dark", 0)
    }

    fun colorSecondaryLight(): Preference<Int> {
        return preferenceStore.getInt("color_secondary_light", 0)
    }

    fun colorSecondaryDark(): Preference<Int> {
        return preferenceStore.getInt("color_secondary_dark", 0)
    }

    fun colorBarsLight(): Preference<Int> {
        return preferenceStore.getInt("color_bar_light", 0)
    }

    fun colorBarsDark(): Preference<Int> {
        return preferenceStore.getInt("color_bar_dark", 0)
    }

    fun confirmExit(): Preference<Boolean> {
        return preferenceStore.getBoolean("confirm_exit", false)
    }

    fun hideBottomBarOnScroll(): Preference<Boolean> {
        return preferenceStore.getBoolean("hide_bottom_bar_on_scroll", true)
    }

    fun language(): Preference<String> {
        return preferenceStore.getString("language", "")
    }

    fun dateFormat(): Preference<String> {
        return preferenceStore.getString("date_format", "")
    }

    fun downloadedOnly(): Preference<Boolean> {
        return preferenceStore.getBoolean("downloaded_only", false)
    }

    fun incognitoMode(): Preference<Boolean> {
        return preferenceStore.getBoolean("incognito_mode", false)
    }

}

