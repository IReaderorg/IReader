package ireader.presentation.ui.video.component.cores

import androidx.annotation.FontRes
import androidx.media3.ui.CaptionStyleCompat

const val SUBTITLE_KEY = "subtitle_settings"
const val SUBTITLE_AUTO_SELECT_KEY = "subs_auto_select"
const val SUBTITLE_DOWNLOAD_KEY = "subs_auto_download"

data class SaveCaptionStyle(
        var foregroundColor: Int,
        var backgroundColor: Int,
        var windowColor: Int,

        var edgeType: Int,
        var edgeColor: Int,
        @FontRes
        var typeface: Int?,
        var typefaceFilePath: String?,
        /**in dp**/
        var elevation: Int,
        /**in sp**/
        var fixedTextSize: Float?,
        var removeCaptions: Boolean = false,
        var removeBloat: Boolean = true,
        /** Apply caps lock to the text **/
        var upperCase: Boolean = false,
)

data class SubtitleFile(val lang: String, val url: String)