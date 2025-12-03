package ireader.data.util

import android.content.Context
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of AppDataDirectory
 */
actual object AppDataDirectory : KoinComponent {
    private val context: Context by inject()
    
    private val appDir: Path by lazy {
        context.filesDir.absolutePath.toPath()
    }
    
    actual fun getPath(): Path = appDir
    
    actual fun getPathString(): String = appDir.toString()
}
