package ireader.presentation.ui.video.component.cores

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.text.SubtitleDecoderFactory
import androidx.media3.extractor.text.SubtitleDecoder
import androidx.media3.extractor.text.SubtitleInputBuffer
import androidx.media3.extractor.text.SubtitleOutputBuffer
import androidx.media3.extractor.text.ssa.SsaDecoder
import androidx.media3.extractor.text.subrip.SubripDecoder
import androidx.media3.extractor.text.ttml.TtmlDecoder
import androidx.media3.extractor.text.webvtt.WebvttDecoder
import ireader.presentation.R
import org.mozilla.universalchardet.UniversalDetector
import java.nio.ByteBuffer
import java.nio.charset.Charset

class CustomDecoder : SubtitleDecoder {
    companion object {
        fun updateForcedEncoding(context: Context) {
            val settingsManager = PreferenceManager.getDefaultSharedPreferences(context)
            val value = settingsManager.getString(
                context.getString(R.string.subtitles_encoding_key),
                null
            )
            overrideEncoding = if (value.isNullOrBlank()) {
                null
            } else {
                value
            }
        }

        private const val UTF_8 = "UTF-8"
        private const val TAG = "CustomDecoder"
        private var overrideEncoding: String? = null
        var regexSubtitlesToRemoveCaptions = false
        var regexSubtitlesToRemoveBloat = false
        var uppercaseSubtitles = false
        val bloatRegex =
            listOf(
                Regex(
                    """Support\s+us\s+and\s+become\s+VIP\s+member\s+to\s+remove\s+all\s+ads\s+from\s+(www\.|)OpenSubtitles(\.org|)""",
                    RegexOption.IGNORE_CASE
                ),
                Regex(
                    """Please\s+rate\s+this\s+subtitle\s+at\s+.*\s+Help\s+other\s+users\s+to\s+choose\s+the\s+best\s+subtitles""",
                    RegexOption.IGNORE_CASE
                ),
                Regex(
                    """Contact\s(www\.|)OpenSubtitles(\.org|)\s+today""",
                    RegexOption.IGNORE_CASE
                ),
                Regex(
                    """Advertise\s+your\s+product\s+or\s+brand\s+here""",
                    RegexOption.IGNORE_CASE
                ),
            )
        val captionRegex = listOf(Regex("""(-\s?|)[\[({][\w\d\s]*?[])}]\s*"""))

        //https://emptycharacter.com/
        //https://www.fileformat.info/info/unicode/char/200b/index.htm
        fun trimStr(string: String): String {
            return string.trimStart().trim('\uFEFF', '\u200B').replace(
                Regex("[\u00A0\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u205F]"),
                " "
            )
        }
    }

    private var realDecoder: SubtitleDecoder? = null

    override fun getName(): String {
        return realDecoder?.name ?: this::javaClass.name
    }

    override fun dequeueInputBuffer(): SubtitleInputBuffer {
        Log.i(TAG, "dequeueInputBuffer")
        return realDecoder?.dequeueInputBuffer() ?: SubtitleInputBuffer()
    }

    private fun getStr(byteArray: ByteArray): Pair<String, Charset> {
        val encoding = try {
            val encoding = overrideEncoding ?: run {
                val detector = UniversalDetector()

                detector.handleData(byteArray, 0, byteArray.size)
                detector.dataEnd()

                detector.detectedCharset // "windows-1256"
            }

            Log.i(
                TAG,
                "Detected encoding with charset $encoding and override = $overrideEncoding"
            )
            encoding ?: UTF_8
        } catch (e: Exception) {
            Log.e(TAG, "Failed to detect encoding throwing error")
            ireader.core.log.Log.error(e)
            UTF_8
        }

        return try {
            val set = charset(encoding)
            Pair(String(byteArray, set), set)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse using encoding $encoding")
            ireader.core.log.Log.error(e)
            Pair(byteArray.decodeToString(), charset(UTF_8))
        }
    }

    private fun getStr(input: SubtitleInputBuffer): String? {
        try {
            val data = input.data ?: return null
            data.position(0)
            val fullDataArr = ByteArray(data.remaining())
            data.get(fullDataArr)
            return trimStr(getStr(fullDataArr).first)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse text returning plain data")
            ireader.core.log.Log.error(e)
            return null
        }
    }

    private fun SubtitleInputBuffer.setSubtitleText(text: String) {
//        println("Set subtitle text -----\n$text\n-----")
        this.data = ByteBuffer.wrap(text.toByteArray(charset(UTF_8)))
    }

    override fun queueInputBuffer(inputBuffer: SubtitleInputBuffer) {
        Log.i(TAG, "queueInputBuffer")
        try {
            val inputString = getStr(inputBuffer)
            if (realDecoder == null && !inputString.isNullOrBlank()) {
                var str: String = inputString
                // this way we read the subtitle file and decide what decoder to use instead of relying on mimetype
                Log.i(TAG, "Got data from queueInputBuffer")
                //https://github.com/LagradOst/CloudStream-2/blob/ddd774ee66810137ff7bd65dae70bcf3ba2d2489/CloudStreamForms/CloudStreamForms/Script/MainChrome.cs#L388
                realDecoder = when {
                    str.startsWith("WEBVTT", ignoreCase = true) -> WebvttDecoder()
                    str.startsWith("<?xml version=\"", ignoreCase = true) -> TtmlDecoder()
                    (str.startsWith(
                        "[Script Info]",
                        ignoreCase = true
                    ) || str.startsWith("Title:", ignoreCase = true)) -> SsaDecoder()
                    str.startsWith("1", ignoreCase = true) -> SubripDecoder()
                    else -> null
                }
                Log.i(
                    TAG,
                    "Decoder selected: $realDecoder"
                )
                realDecoder?.let { decoder ->
                    decoder.dequeueInputBuffer()?.let { buff ->
                        if (decoder !is SsaDecoder) {
                            if (regexSubtitlesToRemoveCaptions)
                                captionRegex.forEach { rgx ->
                                    str = str.replace(rgx, "\n")
                                }
                            if (regexSubtitlesToRemoveBloat)
                                bloatRegex.forEach { rgx ->
                                    str = str.replace(rgx, "\n")
                                }
                        }
                        buff.setSubtitleText(str)
                        decoder.queueInputBuffer(buff)
                        Log.i(
                            TAG,
                            "Decoder queueInputBuffer successfully"
                        )
                    }

                }
            } else {
                Log.i(
                    TAG,
                    "Decoder else queueInputBuffer successfully"
                )

                if (!inputString.isNullOrBlank()) {
                    var str: String = inputString
                    if (realDecoder !is SsaDecoder) {
                        if (regexSubtitlesToRemoveCaptions)
                            captionRegex.forEach { rgx ->
                                str = str.replace(rgx, "\n")
                            }
                        if (regexSubtitlesToRemoveBloat)
                            bloatRegex.forEach { rgx ->
                                str = str.replace(rgx, "\n")
                            }
                        if (uppercaseSubtitles) {
                            str = str.uppercase()
                        }
                    }
                    inputBuffer.setSubtitleText(str)
                }

                realDecoder?.queueInputBuffer(inputBuffer)
            }
        } catch (e: Exception) {
            ireader.core.log.Log.error(e)
        }
    }

    override fun dequeueOutputBuffer(): SubtitleOutputBuffer? {
        return realDecoder?.dequeueOutputBuffer()
    }

    override fun flush() {
        realDecoder?.flush()
    }

    override fun release() {
        realDecoder?.release()
    }

    override fun setPositionUs(positionUs: Long) {
        realDecoder?.setPositionUs(positionUs)
    }
}

/** See https://github.com/google/ExoPlayer/blob/release-v2/library/core/src/main/java/com/google/android/exoplayer2/text/SubtitleDecoderFactory.java */
class CustomSubtitleDecoderFactory : SubtitleDecoderFactory {

    override fun supportsFormat(format: androidx.media3.common.Format): Boolean {
        return listOf(
                MimeTypes.TEXT_VTT,
                MimeTypes.TEXT_SSA,
                MimeTypes.APPLICATION_TTML,
                MimeTypes.APPLICATION_MP4VTT,
                MimeTypes.APPLICATION_SUBRIP,
        ).contains(format.sampleMimeType)
    }

    override fun createDecoder(format: androidx.media3.common.Format): SubtitleDecoder {
        return CustomDecoder()
    }

}