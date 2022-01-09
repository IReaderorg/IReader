package ir.kazemcodes.infinity.data.repository

import android.content.SharedPreferences
import javax.inject.Inject

class PreferencesHelper @Inject constructor(
    pref : SharedPreferences
){
    companion object PreferenceKeys {
        const val USER_DATA = "user_data"
        const val SAVED_FONT_SIZE_PREFERENCES = "reader_font_size"
        const val SAVED_FONT_PREFERENCES = "reader_font_family"
        const val SAVED_BRIGHTNESS_PREFERENCES = "reader_brightness"
        const val SAVED_LATEST_CHAPTER_KEY = "last_chapter_key"

        const val SAVED_LIBRARY_LAYOUT_KEY = "library_layout_type"
        const val SAVED_BROWSE_LAYOUT_KEY = "browser_layout_type"

        /** Setting Pref**/
        const val SAVED_DOH_KEY = "SAVED_DOH_KEY"


    }
    val fontScale = pref.getInt(SAVED_FONT_SIZE_PREFERENCES, 18)
}