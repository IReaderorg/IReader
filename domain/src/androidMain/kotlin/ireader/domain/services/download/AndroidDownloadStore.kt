package ireader.domain.services.download

import android.content.Context
import ireader.core.log.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Android implementation of DownloadStore using SharedPreferences.
 * Persists the download queue to survive app restarts.
 */
class AndroidDownloadStore(
    private val context: Context
) : DownloadStore {
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
    
    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    override suspend fun saveQueue(downloads: List<DownloadQueueItem>) {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = json.encodeToString(downloads)
                prefs.edit()
                    .putString(KEY_QUEUE, jsonString)
                    .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                    .apply()
                Log.debug { "DownloadStore: Saved ${downloads.size} downloads to queue" }
            } catch (e: Exception) {
                Log.error { "DownloadStore: Failed to save queue - ${e.message}" }
            }
        }
    }
    
    override suspend fun restoreQueue(): List<DownloadQueueItem> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = prefs.getString(KEY_QUEUE, null)
                if (jsonString.isNullOrEmpty()) {
                    Log.debug { "DownloadStore: No persisted queue found" }
                    return@withContext emptyList()
                }
                
                val items = json.decodeFromString<List<DownloadQueueItem>>(jsonString)
                Log.debug { "DownloadStore: Restored ${items.size} downloads from queue" }
                items
            } catch (e: Exception) {
                Log.error { "DownloadStore: Failed to restore queue, returning empty - ${e.message}" }
                // Clear corrupted data
                clear()
                emptyList()
            }
        }
    }
    
    override suspend fun clear() {
        withContext(Dispatchers.IO) {
            try {
                prefs.edit()
                    .remove(KEY_QUEUE)
                    .remove(KEY_TIMESTAMP)
                    .apply()
                Log.debug { "DownloadStore: Cleared queue" }
            } catch (e: Exception) {
                Log.error { "DownloadStore: Failed to clear queue - ${e.message}" }
            }
        }
    }
    
    override suspend fun hasPersistedQueue(): Boolean {
        return withContext(Dispatchers.IO) {
            prefs.contains(KEY_QUEUE) && !prefs.getString(KEY_QUEUE, null).isNullOrEmpty()
        }
    }
    
    companion object {
        private const val PREFS_NAME = "download_queue"
        private const val KEY_QUEUE = "queue"
        private const val KEY_TIMESTAMP = "timestamp"
    }
}
