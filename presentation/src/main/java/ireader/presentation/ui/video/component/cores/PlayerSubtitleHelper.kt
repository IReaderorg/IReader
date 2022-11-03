package ireader.presentation.ui.video.component.cores

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.media3.common.MimeTypes
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.SubtitleView
import ireader.presentation.ui.video.component.cores.UIHelper.toPx
import java.io.File

enum class SubtitleStatus {
    IS_ACTIVE,
    REQUIRES_RELOAD,
    NOT_FOUND,
}

enum class SubtitleOrigin {
    URL,
    DOWNLOADED_FILE,
    EMBEDDED_IN_VIDEO
}

/**
 * @param name To be displayed in the player
 * @param url Url for the subtitle, when EMBEDDED_IN_VIDEO this variable is used as the real backend language
 * @param headers if empty it will use the base onlineDataSource headers else only the specified headers
 * */
data class SubtitleData(
    val name: String,
    val url: String,
    val origin: SubtitleOrigin,
    val mimeType: String,
    val headers: Map<String, String>
)

class PlayerSubtitleHelper {
    var subView: SubtitleView? = null
    var subHolder: FrameLayout? = null

    private var activeSubtitles: Set<SubtitleData> = emptySet()
    private var allSubtitles: Set<SubtitleData> = emptySet()


    fun getAllSubtitles(): Set<SubtitleData> {
        return allSubtitles
    }

    fun setActiveSubtitles(list: Set<SubtitleData>) {
        activeSubtitles = list
    }

    fun setAllSubtitles(list: Set<SubtitleData>) {
        allSubtitles = list
    }

    private var subStyle: SaveCaptionStyle? = null
    private var subtitleView: SubtitleView? = null

    companion object {
        fun String.toSubtitleMimeType(): String {
            return when {
                endsWith("vtt", true) -> MimeTypes.TEXT_VTT
                endsWith("srt", true) -> MimeTypes.APPLICATION_SUBRIP
                endsWith("xml", true) || endsWith("ttml", true) -> MimeTypes.APPLICATION_TTML
                else -> MimeTypes.APPLICATION_SUBRIP
            }
        }

        fun getSubtitleData(subtitleFile: SubtitleFile): SubtitleData {
            return SubtitleData(
                name = subtitleFile.lang,
                url = subtitleFile.url,
                origin = SubtitleOrigin.URL,
                mimeType = subtitleFile.url.toSubtitleMimeType(),
                headers = emptyMap()
            )
        }
        fun Context.fromSaveToStyle(data: SaveCaptionStyle): CaptionStyleCompat {
            return CaptionStyleCompat(
                    data.foregroundColor,
                    data.backgroundColor,
                    data.windowColor,
                    data.edgeType,
                    data.edgeColor,
                    data.typefaceFilePath?.let {
                        try {
                            // RuntimeException: Font asset not found
                            Typeface.createFromFile(File(it))
                        } catch (e: Exception) {
                            null
                        }
                    } ?: data.typeface?.let {
                        ResourcesCompat.getFont(
                                this,
                                it
                        )
                    }
                    ?: Typeface.SANS_SERIF
            )
        }
    }

    fun subtitleStatus(sub : SubtitleData?): SubtitleStatus {
        if(activeSubtitles.contains(sub)) {
            return SubtitleStatus.IS_ACTIVE
        }
        if(allSubtitles.contains(sub)) {
            return SubtitleStatus.REQUIRES_RELOAD
        }
        return SubtitleStatus.NOT_FOUND
    }

    fun setSubStyle(style: SaveCaptionStyle) {
        subtitleView?.context?.let { ctx ->
            subStyle = style
            Log.i("TAG","SET STYLE = $style")
            subtitleView?.setStyle(ctx.fromSaveToStyle(style))
            subtitleView?.translationY = -style.elevation.toPx.toFloat()
            val size = style.fixedTextSize
            if (size != null) {
                subtitleView?.setFixedTextSize(TypedValue.COMPLEX_UNIT_SP, size)
            } else {
                subtitleView?.setUserDefaultTextSize()
            }
        }
    }

    fun initSubtitles(subView: SubtitleView?, subHolder: FrameLayout?, style: SaveCaptionStyle?) {
        subtitleView = subView
        subView?.let { sView ->
            (sView.parent as ViewGroup?)?.removeView(sView)
            subHolder?.addView(sView)
        }
        style?.let {
            setSubStyle(it)
        }
    }
}