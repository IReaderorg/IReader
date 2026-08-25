package ireader.domain.models.prefs

import ireader.domain.models.common.AlignmentModel
import ireader.domain.models.common.TextAlignmentModel
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

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
        ALWAYS(UiText.MStringResource(Res.string.lock_always)),
        INCOGNITO(UiText.MStringResource(Res.string.pref_incognito_mode)),
        NEVER(UiText.MStringResource(Res.string.lock_never)),
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
        LocalInstaller,
        HybridInstaller;
        
        companion object {
            // Alias for clearer naming
            val PackageInstaller = AndroidPackageManager
        }
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
fun PreferenceValues.PreferenceAlignment.mapAlignment() : AlignmentModel? {
    return when(this) {
        PreferenceValues.PreferenceAlignment.TopLeft -> AlignmentModel.TOP_END
        PreferenceValues.PreferenceAlignment.BottomLeft -> AlignmentModel.BOTTOM_END
        else -> null
    }
}
fun mapTextAlign(textAlign: PreferenceValues.PreferenceTextAlignment): TextAlignmentModel {
    return when (textAlign) {
        PreferenceValues.PreferenceTextAlignment.Center -> TextAlignmentModel.CENTER
        PreferenceValues.PreferenceTextAlignment.Right -> TextAlignmentModel.RIGHT
        PreferenceValues.PreferenceTextAlignment.Left -> TextAlignmentModel.LEFT
        PreferenceValues.PreferenceTextAlignment.Justify -> TextAlignmentModel.JUSTIFY
        PreferenceValues.PreferenceTextAlignment.Hide -> TextAlignmentModel.JUSTIFY
    }
}