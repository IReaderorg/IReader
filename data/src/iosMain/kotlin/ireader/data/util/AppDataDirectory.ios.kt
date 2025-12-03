package ireader.data.util

import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.*

/**
 * iOS implementation of AppDataDirectory
 */
actual object AppDataDirectory {
    private val appDir: Path by lazy {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        )
        val documentsDir = (paths.firstOrNull() as? String) ?: NSTemporaryDirectory()
        "$documentsDir/IReader".toPath()
    }
    
    actual fun getPath(): Path = appDir
    
    actual fun getPathString(): String = appDir.toString()
}
