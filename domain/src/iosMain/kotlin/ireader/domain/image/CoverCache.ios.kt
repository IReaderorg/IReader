package ireader.domain.image

import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS implementation of CoverCache
 */
actual class CoverCache actual constructor(context: Any) {
    
    private val cacheDir: Path by lazy {
        val paths = NSFileManager.defaultManager.URLsForDirectory(
            NSCachesDirectory,
            NSUserDomainMask
        )
        val cachePath = (paths.firstOrNull() as? platform.Foundation.NSURL)?.path ?: ""
        "$cachePath/covers".toPath()
    }
    
    actual fun getCoverFile(bookId: Long): Path {
        return cacheDir.resolve("$bookId.jpg")
    }
    
    actual fun getCustomCoverFile(bookId: Long): Path {
        return cacheDir.resolve("${bookId}_custom.jpg")
    }
    
    actual fun deleteFromCache(bookId: Long) {
        val fileManager = NSFileManager.defaultManager
        val coverPath = getCoverFile(bookId).toString()
        val customPath = getCustomCoverFile(bookId).toString()
        
        fileManager.removeItemAtPath(coverPath, null)
        fileManager.removeItemAtPath(customPath, null)
    }
    
    actual fun clearCache() {
        val fileManager = NSFileManager.defaultManager
        fileManager.removeItemAtPath(cacheDir.toString(), null)
        fileManager.createDirectoryAtPath(cacheDir.toString(), true, null, null)
    }
}
