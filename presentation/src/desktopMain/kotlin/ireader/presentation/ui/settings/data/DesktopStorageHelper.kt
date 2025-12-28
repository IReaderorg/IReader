package ireader.presentation.ui.settings.data

import ireader.core.log.Log
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Desktop-specific implementation of StorageHelper
 */
class DesktopStorageHelper(
    private val cacheDir: File = File(System.getProperty("user.home"), ".ireader/cache"),
    private val dataDir: File = File(System.getProperty("user.home"), ".ireader")
) : StorageHelper {
    
    override fun getAvailableStorage(): Long {
        return try {
            val store = Files.getFileStore(Paths.get(dataDir.absolutePath))
            store.usableSpace
        } catch (e: Exception) {
            Log.error(e, "Failed to get available storage")
            0L
        }
    }
    
    override fun getTotalStorage(): Long {
        return try {
            val store = Files.getFileStore(Paths.get(dataDir.absolutePath))
            store.totalSpace
        } catch (e: Exception) {
            Log.error(e, "Failed to get total storage")
            0L
        }
    }
    
    override fun getImageCacheSize(): Long {
        return try {
            val imageCacheDir = File(cacheDir, "image_cache")
            val coilCacheDir = File(cacheDir, "coil_cache")
            
            var size = 0L
            if (imageCacheDir.exists()) {
                size += calculateDirectorySize(imageCacheDir)
            }
            if (coilCacheDir.exists()) {
                size += calculateDirectorySize(coilCacheDir)
            }
            size
        } catch (e: Exception) {
            Log.error(e, "Failed to get image cache size")
            0L
        }
    }
    
    override fun getNetworkCacheSize(): Long {
        return try {
            val httpCacheDir = File(cacheDir, "http_cache")
            if (httpCacheDir.exists()) {
                calculateDirectorySize(httpCacheDir)
            } else {
                0L
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to get network cache size")
            0L
        }
    }
    
    override fun clearNetworkCache() {
        try {
            val httpCacheDir = File(cacheDir, "http_cache")
            if (httpCacheDir.exists()) {
                httpCacheDir.deleteRecursively()
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to clear network cache")
        }
    }
    
    override fun clearOldestCacheFiles(daysOld: Int) {
        try {
            val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
            
            if (cacheDir.exists()) {
                cacheDir.walkTopDown().forEach { file ->
                    if (file.isFile && file.lastModified() < cutoffTime) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to clear oldest cache files")
        }
    }
    
    override fun scheduleCleanup(intervalDays: Int) {
        // Desktop doesn't have WorkManager, cleanup is done on app startup
        Log.debug { "Desktop cleanup scheduled for every $intervalDays days (runs on app startup)" }
    }
    
    override fun cancelScheduledCleanup() {
        // No-op on desktop
        Log.debug { "Desktop cleanup scheduling cancelled" }
    }
    
    override fun getDataUsageStats(): DataUsageStats {
        // Desktop doesn't track data usage by network type
        // Return zeros or read from a local file if implemented
        return DataUsageStats(
            totalDownloaded = 0L,
            imagesDownloaded = 0L,
            chaptersDownloaded = 0L,
            metadataDownloaded = 0L,
            wifiUsage = 0L,
            mobileUsage = 0L
        )
    }
    
    private fun calculateDirectorySize(directory: File): Long {
        var size = 0L
        directory.walkTopDown().forEach { file ->
            if (file.isFile) {
                size += file.length()
            }
        }
        return size
    }
}
