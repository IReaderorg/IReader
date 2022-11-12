package ireader.presentation.ui.video.component.cores

import android.content.Context
import android.graphics.Typeface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.res.ResourcesCompat
import androidx.media3.common.MimeTypes
import androidx.media3.ui.CaptionStyleCompat
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

    var activeSubtitles: MutableState<Set<SubtitleData>> = mutableStateOf(emptySet())
        private set
    var allSubtitles: MutableState<Set<SubtitleData>> = mutableStateOf(emptySet())
        private set
//
//    fun getAllSubtitles(): List<SubtitleData> {
//        return allSubtitles.value.toList()
//    }
//    fun getActiveSubtitles(): List<SubtitleData> {
//        return activeSubtitles.value.toList()
//    }

    var internalSubtitles : MutableState<Set<SubtitleData>> = mutableStateOf(emptySet())
        private set

    fun setActiveSubtitles(list: List<SubtitleData>) {
        activeSubtitles.value = (list + internalSubtitles.value).toSet()
    }

    fun setAllSubtitles(list: List<SubtitleData>) {
        allSubtitles.value = (list + internalSubtitles.value).toSet()
    }

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
        if(activeSubtitles.value.contains(sub)) {
            return SubtitleStatus.IS_ACTIVE
        }
        if(allSubtitles.value.contains(sub)) {
            return SubtitleStatus.REQUIRES_RELOAD
        }
        return SubtitleStatus.NOT_FOUND
    }


}