package ireader.domain.services.download

import ireader.core.log.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Desktop implementation of DownloadStore using file-based storage.
 * Persists the download queue to survive app restarts.
 */
class DesktopDownloadStore : DownloadStore {
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
    
    private val configDir: File by lazy {
        val userHome = System.getProperty("user.home")
        File(userHome, ".ireader/config").also { it.mkdirs() }
    }
    
    private val queueFile: File
        get() = File(configDir, QUEUE_FILE_NAME)
    
    override suspend fun saveQueue(downloads: List<DownloadQueueItem>) {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = json.encodeToString(downloads)
                queueFile.writeText(jsonString)
                Log.debug { "DownloadStore: Saved ${downloads.size} downloads to queue" }
            } catch (e: Exception) {
                Log.error { "DownloadStore: Failed to save queue - ${e.message}" }
            }
        }
    }
    
    override suspend fun restoreQueue(): List<DownloadQueueItem> {
        return withContext(Dispatchers.IO) {
            try {
                if (!queueFile.exists()) {
                    Log.debug { "DownloadStore: No persisted queue found" }
                    return@withContext emptyList()
                }
                
                val jsonString = queueFile.readText()
                if (jsonString.isEmpty()) {
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
                if (queueFile.exists()) {
                    queueFile.delete()
                }
                Log.debug { "DownloadStore: Cleared queue" }
            } catch (e: Exception) {
                Log.error { "DownloadStore: Failed to clear queue - ${e.message}" }
            }
        }
    }
    
    override suspend fun hasPersistedQueue(): Boolean {
        return withContext(Dispatchers.IO) {
            queueFile.exists() && queueFile.length() > 0
        }
    }
    
    companion object {
        private const val QUEUE_FILE_NAME = "download_queue.json"
    }
}
