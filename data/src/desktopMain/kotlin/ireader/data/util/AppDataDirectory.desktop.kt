package ireader.data.util

import okio.Path
import okio.Path.Companion.toPath

/**
 * Desktop implementation of AppDataDirectory
 */
actual object AppDataDirectory {
    private val appDir: Path by lazy {
        val userHome = System.getProperty("user.home") ?: "."
        "$userHome/.ireader".toPath()
    }
    
    actual fun getPath(): Path = appDir
    
    actual fun getPathString(): String = appDir.toString()
}
