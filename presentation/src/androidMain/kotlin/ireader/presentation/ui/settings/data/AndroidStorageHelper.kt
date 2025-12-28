package ireader.presentation.ui.settings.data

import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ireader.core.log.Log
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Android-specific implementation of StorageHelper
 */
class AndroidStorageHelper(private val context: Context) : StorageHelper {
    
    override fun getAvailableStorage(): Long {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (e: Exception) {
            Log.error(e, "Failed to get available storage")
            0L
        }
    }
    
    override fun getTotalStorage(): Long {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            stat.blockCountLong * stat.blockSizeLong
        } catch (e: Exception) {
            Log.error(e, "Failed to get total storage")
            0L
        }
    }
    
    override fun getImageCacheSize(): Long {
        return try {
            val cacheDir = context.cacheDir
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
            val cacheDir = context.cacheDir
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
            val cacheDir = context.cacheDir
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
            val cacheDir = context.cacheDir
            
            cacheDir.walkTopDown().forEach { file ->
                if (file.isFile && file.lastModified() < cutoffTime) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to clear oldest cache files")
        }
    }
    
    override fun scheduleCleanup(intervalDays: Int) {
        try {
            val workRequest = PeriodicWorkRequestBuilder<CacheCleanupWorker>(
                intervalDays.toLong(), TimeUnit.DAYS
            ).build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                CLEANUP_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to schedule cleanup")
        }
    }
    
    override fun cancelScheduledCleanup() {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(CLEANUP_WORK_NAME)
        } catch (e: Exception) {
            Log.error(e, "Failed to cancel scheduled cleanup")
        }
    }
    
    override fun getDataUsageStats(): DataUsageStats {
        val prefs = context.getSharedPreferences("data_usage", Context.MODE_PRIVATE)
        return DataUsageStats(
            totalDownloaded = prefs.getLong("total_downloaded", 0L),
            imagesDownloaded = prefs.getLong("images_downloaded", 0L),
            chaptersDownloaded = prefs.getLong("chapters_downloaded", 0L),
            metadataDownloaded = prefs.getLong("metadata_downloaded", 0L),
            wifiUsage = prefs.getLong("wifi_usage", 0L),
            mobileUsage = prefs.getLong("mobile_usage", 0L)
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
    
    companion object {
        private const val CLEANUP_WORK_NAME = "cache_cleanup_work"
    }
}
