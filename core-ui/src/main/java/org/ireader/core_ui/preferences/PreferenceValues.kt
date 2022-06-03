package org.ireader.core_ui.preferences

import org.ireader.core_ui.R

/**
 * This class stores the values for the preferences in the application.
 */
object PreferenceValues {
    enum class ThemeMode {
        System,
        Dark,
        Light,
    }


    enum class SecureScreenMode(val titleResId: Int) {
        ALWAYS(R.string.lock_always),
        INCOGNITO(R.string.pref_incognito_mode),
        NEVER(R.string.lock_never),
    }

    enum class RelativeTime {
        Off,
        Seconds,
        Minutes,
        Hour,
        Day,
        Week;
    }


}
