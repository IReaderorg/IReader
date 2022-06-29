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

    /**
     * Scrollbar selection modes.
     */
    enum class ScrollbarSelectionMode {
        /**
         * Enable selection in the whole scrollbar and thumb
         */
        Full,
        /**
         * Enable selection in the thumb
         */
        Thumb,
        /**
         * Disable selection
         */
        Disabled;
        companion object {
            fun valueOf(ordinal:Int) : ScrollbarSelectionMode {
                return when(ordinal) {
                    Full.ordinal -> Full
                    Thumb.ordinal -> Thumb
                    else -> Disabled
                }
            }
        }

    }

}
