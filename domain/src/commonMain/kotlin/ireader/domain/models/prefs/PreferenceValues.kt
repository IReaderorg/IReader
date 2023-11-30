package ireader.domain.models.prefs

import androidx.compose.ui.Alignment
import ireader.i18n.UiText

/**
 * This class stores the values for the preferences in the application.
 */
object PreferenceValues {
    enum class ThemeMode {
        System,
        Dark,
        Light,
    }

    enum class SecureScreenMode(val titleResId: UiText.MStringResource) {
        ALWAYS(UiText.MStringResource() { xml ->
            xml.lockAlways
        }),
        INCOGNITO(UiText.MStringResource() { xml ->
            xml.prefIncognitoMode
        }),
        NEVER(UiText.MStringResource() { xml ->
            xml.lockNever
        }),
    }

    enum class RelativeTime {
        Off,
        Seconds,
        Minutes,
        Hour,
        Day,
        Week;
    }

    enum class Installer {
        AndroidPackageManager,
        LocalInstaller;
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
            fun valueOf(ordinal: Int): ScrollbarSelectionMode {
                return when (ordinal) {
                    Full.ordinal -> Full
                    Thumb.ordinal -> Thumb
                    else -> Disabled
                }
            }
        }
    }

    enum class PreferenceTextAlignment {
        Right,
        Left,
        Center,
        Justify,
        Hide,

    }

    enum class PreferenceAlignment {
        TopLeft,
        BottomLeft,
        Hide,

    }

    enum class AutomaticBackup {
        Off,
        Every6Hours,
        Every12Hours,
        Daily,
        Every2Days,
        Weekly
    }
}

fun PreferenceValues.PreferenceAlignment.mapAlignment(): Alignment? {
    return when (this) {
        PreferenceValues.PreferenceAlignment.TopLeft -> Alignment.TopEnd
        PreferenceValues.PreferenceAlignment.BottomLeft -> Alignment.BottomEnd
        else -> null
    }
}

fun mapTextAlign(textAlign: PreferenceValues.PreferenceTextAlignment): androidx.compose.ui.text.style.TextAlign {
    return when (textAlign) {
        PreferenceValues.PreferenceTextAlignment.Center -> androidx.compose.ui.text.style.TextAlign.Center
        PreferenceValues.PreferenceTextAlignment.Right -> androidx.compose.ui.text.style.TextAlign.Right
        PreferenceValues.PreferenceTextAlignment.Left -> androidx.compose.ui.text.style.TextAlign.Left
        PreferenceValues.PreferenceTextAlignment.Justify -> androidx.compose.ui.text.style.TextAlign.Justify
        PreferenceValues.PreferenceTextAlignment.Hide -> androidx.compose.ui.text.style.TextAlign.Justify
    }
}