package ireader.presentation.ui.video

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import ireader.presentation.R
import org.koin.core.annotation.Factory
import java.io.File

@Factory
class SubtitleHandler(
    private val context: Context
) {
    val subtitles = Uri.parse(
        ContentResolver.SCHEME_ANDROID_RESOURCE
                + File.pathSeparator + File.separator + File.separator
                + context.packageName
                + File.separator
                + R.raw.sample_subtitle
    )



}