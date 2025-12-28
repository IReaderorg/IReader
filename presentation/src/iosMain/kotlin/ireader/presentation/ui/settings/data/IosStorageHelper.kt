package ireader.presentation.ui.settings.data

import ireader.core.log.Log
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileModificationDate
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL
import platform.Foundation.NSURLVolumeAvailableCapacityForImportantUsageKey
import platform.Foundation.NSURLVolumeTotalCapacityKey
import platform.Foundation.timeIntervalSince1970

/**
 * iOS-specific implementation of StorageHelper.
 * Uses NSFileManager for file operations.
 */
@OptIn(ExperimentalForeignApi::class)
class IosStorageHelper : StorageHelper {
    
    private val fileManager = NSFileManager.defaultManager
    private val cachesDirectory: String by lazy {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSCachesDirectory,
            NSUserDomainMask,
            true
        )
        (paths.firstOrNull() as? String) ?: ""
    }
    
    override fun getAvailableStorage(): Long {
        return try {
            val url = NSURL.fileURLWithPath(cachesDirectory)
            val values = url.resourceValuesForKeys(
                listOf(NSURLVolumeAvailableCapacityForImportantUsageKey),
                error = null
            )
            (values?.get(NSURLVolumeAvailableCapacityForImportantUsageKey) as? NSNumber)?.longValue ?: 0L
        } catch (e: Exception) {
            Log.error(e, "Failed to get available storage")
            0L
        }
    }
    
    override fun getTotalStorage(): Long {
        return try {
            val url = NSURL.fileURLWithPath(cachesDirectory)
            val values = url.resourceValuesForKeys(
                listOf(NSURLVolumeTotalCapacityKey),
                error = null
            )
            (values?.get(NSURLVolumeTotalCapacityKey) as? NSNumber)?.longValue ?: 0L
        } catch (e: Exception) {
            Log.error(e, "Failed to get total storage")
            0L
        }
    }
    
    override fun getImageCacheSize(): Long {
        return try {
            val imageCachePath = "$cachesDirectory/image_cache"
            val coilCachePath = "$cachesDirectory/coil_cache"
            calculateDirectorySize(imageCachePath) + calculateDirectorySize(coilCachePath)
        } catch (e: Exception) {
            Log.error(e, "Failed to get image cache size")
            0L
        }
    }
    
    override fun getNetworkCacheSize(): Long {
        return try {
            val httpCachePath = "$cachesDirectory/http_cache"
            calculateDirectorySize(httpCachePath)
        } catch (e: Exception) {
            Log.error(e, "Failed to get network cache size")
            0L
        }
    }
    
    override fun clearNetworkCache() {
        try {
            val httpCachePath = "$cachesDirectory/http_cache"
            if (fileManager.fileExistsAtPath(httpCachePath)) {
                fileManager.removeItemAtPath(httpCachePath, error = null)
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to clear network cache")
        }
    }
    
    override fun clearOldestCacheFiles(daysOld: Int) {
        try {
            val cutoffTimeSeconds = (currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)) / 1000.0
            clearOldFilesInDirectory(cachesDirectory, cutoffTimeSeconds)
        } catch (e: Exception) {
            Log.error(e, "Failed to clear oldest cache files")
        }
    }
    
    private fun clearOldFilesInDirectory(path: String, cutoffTimeSeconds: Double) {
        val contents = fileManager.contentsOfDirectoryAtPath(path, error = null) ?: return
        
        for (item in contents) {
            val itemPath = "$path/$item"
            
            if (fileManager.fileExistsAtPath(itemPath)) {
                val attrs = fileManager.attributesOfItemAtPath(itemPath, error = null)
                val modDate = attrs?.get(NSFileModificationDate) as? NSDate
                
                if (modDate != null && modDate.timeIntervalSince1970 < cutoffTimeSeconds) {
                    fileManager.removeItemAtPath(itemPath, error = null)
                }
            }
        }
    }
    
    override fun scheduleCleanup(intervalDays: Int) {
        // iOS doesn't have WorkManager equivalent
        // Background tasks would require BGTaskScheduler which needs app delegate setup
        Log.debug { "iOS: Cleanup scheduling not implemented (requires BGTaskScheduler)" }
    }
    
    override fun cancelScheduledCleanup() {
        // No-op on iOS
        Log.debug { "iOS: Cleanup cancellation not implemented" }
    }
    
    override fun getDataUsageStats(): DataUsageStats {
        // iOS doesn't provide easy access to per-app network usage
        return DataUsageStats(
            totalDownloaded = 0L,
            imagesDownloaded = 0L,
            chaptersDownloaded = 0L,
            metadataDownloaded = 0L,
            wifiUsage = 0L,
            mobileUsage = 0L
        )
    }
    
    private fun calculateDirectorySize(path: String): Long {
        if (!fileManager.fileExistsAtPath(path)) return 0L
        
        var size = 0L
        val enumerator = fileManager.enumeratorAtPath(path) ?: return 0L
        
        var file = enumerator.nextObject()
        while (file != null) {
            val filePath = "$path/$file"
            val attrs = fileManager.attributesOfItemAtPath(filePath, error = null)
            val fileSize = (attrs?.get(NSFileSize) as? NSNumber)?.longValue ?: 0L
            size += fileSize
            file = enumerator.nextObject()
        }
        
        return size
    }
    
    private fun currentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }
}
