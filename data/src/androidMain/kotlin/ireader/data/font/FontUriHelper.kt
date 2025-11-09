package ireader.data.font

import ireader.domain.models.common.Uri
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.sink
import okio.source
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import android.content.Context

/**
 * Android implementation - uses ContentResolver to read from Uri
 */
actual fun copyFontFromUri(uri: Uri, destPath: Path) {
    val context = FontUriHelperAndroid.context
        ?: throw IllegalStateException("Context not initialized for FontUriHelper")
    
    val fileSystem = FileSystem.SYSTEM
    val inputStream = context.contentResolver.openInputStream(uri.androidUri)
        ?: throw IllegalArgumentException("Cannot open input stream from URI")
    
    inputStream.use { input ->
        fileSystem.sink(destPath).buffer().use { output ->
            input.source().buffer().use { source ->
                output.writeAll(source)
            }
        }
    }
}

/**
 * Helper object to hold Android context
 */
object FontUriHelperAndroid : KoinComponent {
    val context: Context? by inject()
}
