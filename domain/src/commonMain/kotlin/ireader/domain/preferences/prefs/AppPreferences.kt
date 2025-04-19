package ireader.domain.preferences.prefs

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.models.library.LibrarySort
import ireader.domain.preferences.models.ReaderColors
import ireader.domain.preferences.models.prefs.IReaderVoice
import ireader.domain.preferences.models.prefs.asColor
import ireader.domain.preferences.models.prefs.asReaderTheme
import ireader.domain.preferences.models.prefs.asVoice

class AppPreferences(
    private val preferenceStore: PreferenceStore,
) {
    companion object PreferenceKeys {
        val DEFAULT_VERSION = 1
        const val SAVED_LIBRARY_LAYOUT_KEY = "library_layout_type"
        const val SAVED_BROWSE_LAYOUT_KEY = "browser_layout_type"

        const val SORT_LIBRARY_SCREEN = "sort_library_screen"
        const val SORT_DESC_LIBRARY_SCREEN = "sort_desc_library_screen"

        /** Services **/
        const val Last_UPDATE_CHECK = "last_update_check"

        /** Setting Pref**/
        const val SAVED_DOH_KEY = "SAVED_DOH_KEY"
        const val DEFAULT_IMAGE_LOADER = "default_image_loader"
        const val SAVED_BACKGROUND_COLOR = "background_color"
        const val SAVED_FONT_PREFERENCES = "reader_font_family"
        const val DATABASE_VERSION = "database_version"

        enum class Orientation {
            Portrait,
            Landscape,
            Unspecified
        }
    }
    fun readerTheme(): Preference<ReaderColors> {
        return preferenceStore.getLong("readerTheme", 0).asReaderTheme()
    }
    fun textColorReader(): Preference<Color> {
        return preferenceStore.getInt(ReaderPreferences.SAVED_TEXT_COLOR, Color(0xFFE9E9E9).toArgb()).asColor()
    }
     fun backgroundColorReader(): Preference<Color> {
        return preferenceStore.getInt(SAVED_BACKGROUND_COLOR, Color(0xff262626).toArgb()).asColor()
    }
     fun backgroundColorTTS(): Preference<ReaderColors> {
        return preferenceStore.getLong("background_color_tts").asReaderTheme()
    }
    fun libraryLayoutType(): Preference<Long> {
        return preferenceStore.getLong(SAVED_LIBRARY_LAYOUT_KEY, 0)
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
                Orientation.Unspecified.ordinal
        )
    }

    fun exploreLayoutType(): Preference<Long> {
        return preferenceStore.getLong(SAVED_BROWSE_LAYOUT_KEY, 0)
    }
    fun speechVoice(): Preference<IReaderVoice> {
        return preferenceStore.getString(ReaderPreferences.TEXT_READER_SPEECH_VOICE, "").asVoice()
    }

    fun dohStateKey(): Preference<Int> {
        return preferenceStore.getInt(SAVED_DOH_KEY, 0)
    }

    fun appUpdater(): Preference<Boolean> {
        return preferenceStore.getBoolean("app_updater", true)
    }
    fun database_version(): Preference<Int> {
        return preferenceStore.getInt(DATABASE_VERSION, DEFAULT_VERSION)
    }

    fun sortLibraryScreen(): Preference<String> {
        return preferenceStore.getString(SORT_LIBRARY_SCREEN, LibrarySort.Type.LastRead.name)
    }

    fun sortDescLibraryScreen(): Preference<Boolean> {
        return preferenceStore.getBoolean(SORT_DESC_LIBRARY_SCREEN, true)
    }

    fun lastUpdateCheck(): Preference<Long> {
        return preferenceStore.getLong(Last_UPDATE_CHECK, 0)
    }
}
