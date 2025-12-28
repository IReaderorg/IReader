package ireader.presentation.ui.settings.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ireader.core.log.Log
import java.io.File

/**
 * Background worker for automatic cache cleanup
 */
class CacheCleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            Log.info { "Starting automatic cache cleanup" }
            
            val cacheDir = applicationContext.cacheDir
            val cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L) // 7 days
            var deletedCount = 0
            var deletedSize = 0L
            
            cacheDir.walkTopDown().forEach { file ->
                if (file.isFile && file.lastModified() < cutoffTime) {
                    val size = file.length()
                    if (file.delete()) {
                        deletedCount++
                        deletedSize += size
                    }
                }
            }
            
            Log.info { "Cache cleanup completed: deleted $deletedCount files (${deletedSize / 1024}KB)" }
            Result.success()
        } catch (e: Exception) {
            Log.error(e, "Cache cleanup failed")
            Result.failure()
        }
    }
}
