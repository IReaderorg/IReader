package ireader.domain.plugins.offline

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.Path.Companion.toPath
import ireader.core.util.createICoroutineScope
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginUpdateChecker
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Manager for offline plugin caching.
 */
class OfflineCacheManager(
    private val pluginManager: PluginManager,
    private val updateChecker: PluginUpdateChecker,
    private val cacheStorage: CacheStorage,
    private val downloadManager: PluginDownloadManager
) {
    private val scope: CoroutineScope = createICoroutineScope()
    private val mutex = Mutex()
    private var autoDownloadJob: Job? = null
    
    private val _cachedPlugins = MutableStateFlow<List<CachedPlugin>>(emptyList())
    val cachedPlugins: StateFlow<List<CachedPlugin>> = _cachedPlugins.asStateFlow()
    
    private val _downloadTasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val downloadTasks: StateFlow<List<DownloadTask>> = _downloadTasks.asStateFlow()
    
    private val _statistics = MutableStateFlow(CacheStatistics(
        totalCachedPlugins = 0,
        totalCacheSize = 0,
        maxCacheSize = 0,
        usagePercent = 0f,
        pendingDownloads = 0,
        failedDownloads = 0,
        expiredItems = 0,
        lastCleanup = null
    ))
    val statistics: StateFlow<CacheStatistics> = _statistics.asStateFlow()
    
    private val _events = MutableSharedFlow<DownloadEvent>(replay = 0)
    val events: Flow<DownloadEvent> = _events.asSharedFlow()
    
    private var config = CacheConfig()

    /**
     * Initialize the cache manager.
     */
    suspend fun initialize() {
        loadCachedPlugins()
        updateStatistics()
        cleanupExpiredItems()
    }
    
    /**
     * Update cache configuration.
     */
    fun updateConfig(newConfig: CacheConfig) {
        config = newConfig
        if (config.autoDownloadUpdates) {
            startAutoDownload()
        } else {
            stopAutoDownload()
        }
    }
    
    /**
     * Start automatic update downloads.
     */
    fun startAutoDownload() {
        if (!config.enabled || !config.autoDownloadUpdates) return
        
        autoDownloadJob?.cancel()
        autoDownloadJob = scope.launch {
            while (isActive) {
                checkAndDownloadUpdates()
                delay(6 * 60 * 60 * 1000L) // Check every 6 hours
            }
        }
    }
    
    /**
     * Stop automatic downloads.
     */
    fun stopAutoDownload() {
        autoDownloadJob?.cancel()
        autoDownloadJob = null
    }
    
    /**
     * Check for updates and download them.
     */
    suspend fun checkAndDownloadUpdates() {
        if (!config.enabled) return
        
        val updates = updateChecker.checkForUpdatesForCache()
        for (update in updates) {
            if (!isCached(update.pluginId, update.newVersion)) {
                queueDownload(
                    pluginId = update.pluginId,
                    pluginName = update.pluginName,
                    version = update.newVersion,
                    downloadUrl = update.downloadUrl,
                    fileSize = update.fileSize,
                    priority = DownloadPriority.NORMAL
                )
            }
        }
    }
    
    /**
     * Queue a plugin for download.
     */
    suspend fun queueDownload(
        pluginId: String,
        pluginName: String,
        version: String,
        downloadUrl: String,
        fileSize: Long?,
        priority: DownloadPriority = DownloadPriority.NORMAL
    ): DownloadTask {
        val task = DownloadTask(
            id = "download_${currentTimeToLong()}_${(0..999999).random()}",
            pluginId = pluginId,
            pluginName = pluginName,
            version = version,
            downloadUrl = downloadUrl,
            fileSize = fileSize,
            priority = priority,
            status = DownloadStatus.QUEUED,
            progress = 0f,
            downloadedBytes = 0,
            createdAt = currentTimeToLong(),
            startedAt = null,
            completedAt = null,
            errorMessage = null
        )
        
        mutex.withLock {
            _downloadTasks.value = _downloadTasks.value + task
        }
        
        _events.emit(DownloadEvent.Queued(task))
        
        // Start download if under concurrent limit
        processDownloadQueue()
        
        return task
    }
    
    /**
     * Cancel a download.
     */
    suspend fun cancelDownload(taskId: String) {
        downloadManager.cancelDownload(taskId)
        
        mutex.withLock {
            _downloadTasks.value = _downloadTasks.value.map { task ->
                if (task.id == taskId) task.copy(status = DownloadStatus.CANCELLED)
                else task
            }
        }
        
        _events.emit(DownloadEvent.Cancelled(taskId))
    }
    
    /**
     * Pause a download.
     */
    suspend fun pauseDownload(taskId: String) {
        downloadManager.pauseDownload(taskId)
        
        mutex.withLock {
            _downloadTasks.value = _downloadTasks.value.map { task ->
                if (task.id == taskId) task.copy(status = DownloadStatus.PAUSED)
                else task
            }
        }
        
        _events.emit(DownloadEvent.Paused(taskId))
    }
    
    /**
     * Resume a paused download.
     */
    suspend fun resumeDownload(taskId: String) {
        mutex.withLock {
            _downloadTasks.value = _downloadTasks.value.map { task ->
                if (task.id == taskId) task.copy(status = DownloadStatus.QUEUED)
                else task
            }
        }
        
        _events.emit(DownloadEvent.Resumed(taskId))
        processDownloadQueue()
    }
    
    /**
     * Retry a failed download.
     */
    suspend fun retryDownload(taskId: String) {
        mutex.withLock {
            _downloadTasks.value = _downloadTasks.value.map { task ->
                if (task.id == taskId) task.copy(
                    status = DownloadStatus.QUEUED,
                    retryCount = task.retryCount + 1,
                    errorMessage = null
                )
                else task
            }
        }
        
        processDownloadQueue()
    }
    
    /**
     * Install a cached plugin.
     */
    suspend fun installCachedPlugin(pluginId: String, version: String): Result<Unit> {
        val cached = _cachedPlugins.value.find { 
            it.pluginId == pluginId && it.version == version 
        } ?: return Result.failure(IllegalArgumentException("Plugin not cached"))
        
        if (cached.status != CacheStatus.CACHED) {
            return Result.failure(IllegalStateException("Plugin cache is not ready"))
        }
        
        return try {
            // Verify checksum
            val isValid = cacheStorage.verifyChecksum(cached.filePath, cached.checksum)
            if (!isValid) {
                updateCacheStatus(pluginId, version, CacheStatus.CORRUPTED)
                return Result.failure(IllegalStateException("Cache corrupted"))
            }
            
            // Install from cache
            val path = cached.filePath.toPath()
            pluginManager.installPlugin(path)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if a plugin version is cached.
     */
    fun isCached(pluginId: String, version: String): Boolean {
        return _cachedPlugins.value.any { 
            it.pluginId == pluginId && 
            it.version == version && 
            it.status == CacheStatus.CACHED 
        }
    }
    
    /**
     * Get cached plugin info.
     */
    fun getCachedPlugin(pluginId: String, version: String): CachedPlugin? {
        return _cachedPlugins.value.find { 
            it.pluginId == pluginId && it.version == version 
        }
    }
    
    /**
     * Get available updates with cache status.
     */
    suspend fun getAvailableUpdates(): List<PluginUpdateInfo> {
        val updates = updateChecker.checkForUpdatesForCache()
        return updates.map { update ->
            val cached = getCachedPlugin(update.pluginId, update.newVersion)
            PluginUpdateInfo(
                pluginId = update.pluginId,
                pluginName = update.pluginName,
                currentVersion = update.currentVersion,
                currentVersionCode = update.currentVersionCode,
                newVersion = update.newVersion,
                newVersionCode = update.newVersionCode,
                downloadUrl = update.downloadUrl,
                fileSize = update.fileSize,
                changelog = update.changelog,
                isBreakingChange = update.isBreakingChange,
                isCached = cached?.status == CacheStatus.CACHED,
                cachedAt = cached?.cachedAt
            )
        }
    }
    
    /**
     * Clear all cached plugins.
     */
    suspend fun clearCache(): CleanupResult {
        val cached = _cachedPlugins.value
        var freedBytes = 0L
        
        for (plugin in cached) {
            cacheStorage.deleteCache(plugin.filePath)
            freedBytes += plugin.fileSize
        }
        
        mutex.withLock {
            _cachedPlugins.value = emptyList()
        }
        
        updateStatistics()
        
        return CleanupResult(
            removedItems = cached.size,
            freedBytes = freedBytes,
            remainingItems = 0,
            remainingBytes = 0
        )
    }
    
    /**
     * Remove a specific cached plugin.
     */
    suspend fun removeCachedPlugin(pluginId: String, version: String): Result<Unit> {
        val cached = getCachedPlugin(pluginId, version)
            ?: return Result.failure(IllegalArgumentException("Plugin not cached"))
        
        return try {
            cacheStorage.deleteCache(cached.filePath)
            
            mutex.withLock {
                _cachedPlugins.value = _cachedPlugins.value.filter { 
                    !(it.pluginId == pluginId && it.version == version) 
                }
            }
            
            updateStatistics()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cleanup expired and corrupted items.
     */
    suspend fun cleanupExpiredItems(): CleanupResult {
        val now = currentTimeToLong()
        val toRemove = _cachedPlugins.value.filter { cached ->
            cached.status == CacheStatus.EXPIRED ||
            cached.status == CacheStatus.CORRUPTED ||
            (cached.expiresAt != null && cached.expiresAt < now)
        }
        
        var freedBytes = 0L
        for (cached in toRemove) {
            cacheStorage.deleteCache(cached.filePath)
            freedBytes += cached.fileSize
        }
        
        mutex.withLock {
            _cachedPlugins.value = _cachedPlugins.value - toRemove.toSet()
        }
        
        updateStatistics()
        
        return CleanupResult(
            removedItems = toRemove.size,
            freedBytes = freedBytes,
            remainingItems = _cachedPlugins.value.size,
            remainingBytes = _cachedPlugins.value.sumOf { it.fileSize }
        )
    }
    
    /**
     * Cleanup to stay within cache size limit.
     */
    suspend fun enforceStorageLimit(): CleanupResult {
        val currentSize = _cachedPlugins.value.sumOf { it.fileSize }
        if (currentSize <= config.maxCacheSizeBytes) {
            return CleanupResult(0, 0, _cachedPlugins.value.size, currentSize)
        }
        
        // Remove oldest items first
        val sorted = _cachedPlugins.value.sortedBy { it.cachedAt }
        var freedBytes = 0L
        val toRemove = mutableListOf<CachedPlugin>()
        
        for (cached in sorted) {
            if (currentSize - freedBytes <= config.maxCacheSizeBytes) break
            toRemove.add(cached)
            freedBytes += cached.fileSize
            cacheStorage.deleteCache(cached.filePath)
        }
        
        mutex.withLock {
            _cachedPlugins.value = _cachedPlugins.value - toRemove.toSet()
        }
        
        updateStatistics()
        
        return CleanupResult(
            removedItems = toRemove.size,
            freedBytes = freedBytes,
            remainingItems = _cachedPlugins.value.size,
            remainingBytes = _cachedPlugins.value.sumOf { it.fileSize }
        )
    }
    
    private suspend fun loadCachedPlugins() {
        _cachedPlugins.value = cacheStorage.getAllCachedPlugins()
    }
    
    private suspend fun updateStatistics() {
        val cached = _cachedPlugins.value
        val tasks = _downloadTasks.value
        
        _statistics.value = CacheStatistics(
            totalCachedPlugins = cached.count { it.status == CacheStatus.CACHED },
            totalCacheSize = cached.sumOf { it.fileSize },
            maxCacheSize = config.maxCacheSizeBytes,
            usagePercent = if (config.maxCacheSizeBytes > 0) {
                (cached.sumOf { it.fileSize }.toFloat() / config.maxCacheSizeBytes) * 100
            } else 0f,
            pendingDownloads = tasks.count { it.status == DownloadStatus.QUEUED || it.status == DownloadStatus.DOWNLOADING },
            failedDownloads = tasks.count { it.status == DownloadStatus.FAILED },
            expiredItems = cached.count { it.status == CacheStatus.EXPIRED },
            lastCleanup = _statistics.value.lastCleanup
        )
    }
    
    private suspend fun updateCacheStatus(pluginId: String, version: String, status: CacheStatus) {
        mutex.withLock {
            _cachedPlugins.value = _cachedPlugins.value.map { cached ->
                if (cached.pluginId == pluginId && cached.version == version) {
                    cached.copy(status = status)
                } else cached
            }
        }
    }
    
    private suspend fun processDownloadQueue() {
        val activeDownloads = _downloadTasks.value.count { it.status == DownloadStatus.DOWNLOADING }
        if (activeDownloads >= config.maxConcurrentDownloads) return
        
        val nextTasks = _downloadTasks.value
            .filter { it.status == DownloadStatus.QUEUED }
            .sortedByDescending { it.priority.ordinal }
            .take(config.maxConcurrentDownloads - activeDownloads)
        
        for (task in nextTasks) {
            scope.launch {
                startDownload(task)
            }
        }
    }
    
    private suspend fun startDownload(task: DownloadTask) {
        mutex.withLock {
            _downloadTasks.value = _downloadTasks.value.map { t ->
                if (t.id == task.id) t.copy(
                    status = DownloadStatus.DOWNLOADING,
                    startedAt = currentTimeToLong()
                )
                else t
            }
        }
        
        _events.emit(DownloadEvent.Started(task.id))
        
        try {
            val result = downloadManager.download(
                taskId = task.id,
                url = task.downloadUrl,
                onProgress = { progress, downloadedBytes ->
                    scope.launch {
                        updateDownloadProgress(task.id, progress, downloadedBytes)
                        _events.emit(DownloadEvent.Progress(task.id, progress, downloadedBytes))
                    }
                }
            )
            
            result.fold(
                onSuccess = { downloadResult ->
                    val cachedPlugin = CachedPlugin(
                        pluginId = task.pluginId,
                        pluginName = task.pluginName,
                        version = task.version,
                        versionCode = 0, // Would be extracted from manifest
                        cachedAt = currentTimeToLong(),
                        expiresAt = currentTimeToLong() + (config.cacheExpirationDays * 24 * 60 * 60 * 1000L),
                        filePath = downloadResult.filePath,
                        fileSize = downloadResult.fileSize,
                        checksum = downloadResult.checksum,
                        isUpdate = true,
                        currentInstalledVersion = null,
                        downloadUrl = task.downloadUrl,
                        status = CacheStatus.CACHED
                    )
                    
                    cacheStorage.saveCachedPlugin(cachedPlugin)
                    
                    mutex.withLock {
                        _cachedPlugins.value = _cachedPlugins.value + cachedPlugin
                        _downloadTasks.value = _downloadTasks.value.map { t ->
                            if (t.id == task.id) t.copy(
                                status = DownloadStatus.COMPLETED,
                                completedAt = currentTimeToLong(),
                                progress = 1f
                            )
                            else t
                        }
                    }
                    
                    updateStatistics()
                    enforceStorageLimit()
                    
                    _events.emit(DownloadEvent.Completed(task.id, cachedPlugin))
                },
                onFailure = { error ->
                    mutex.withLock {
                        _downloadTasks.value = _downloadTasks.value.map { t ->
                            if (t.id == task.id) t.copy(
                                status = DownloadStatus.FAILED,
                                errorMessage = error.message
                            )
                            else t
                        }
                    }
                    
                    _events.emit(DownloadEvent.Failed(task.id, error.message ?: "Unknown error"))
                }
            )
        } catch (e: Exception) {
            mutex.withLock {
                _downloadTasks.value = _downloadTasks.value.map { t ->
                    if (t.id == task.id) t.copy(
                        status = DownloadStatus.FAILED,
                        errorMessage = e.message
                    )
                    else t
                }
            }
            
            _events.emit(DownloadEvent.Failed(task.id, e.message ?: "Unknown error"))
        }
        
        processDownloadQueue()
    }
    
    private suspend fun updateDownloadProgress(taskId: String, progress: Float, downloadedBytes: Long) {
        mutex.withLock {
            _downloadTasks.value = _downloadTasks.value.map { task ->
                if (task.id == taskId) task.copy(
                    progress = progress,
                    downloadedBytes = downloadedBytes
                )
                else task
            }
        }
    }
}

/**
 * Interface for cache storage operations.
 */
interface CacheStorage {
    suspend fun getAllCachedPlugins(): List<CachedPlugin>
    suspend fun saveCachedPlugin(plugin: CachedPlugin)
    suspend fun deleteCache(filePath: String)
    suspend fun verifyChecksum(filePath: String, expectedChecksum: String): Boolean
    suspend fun getCacheSize(): Long
}

/**
 * Interface for plugin downloads.
 */
interface PluginDownloadManager {
    suspend fun download(
        taskId: String,
        url: String,
        onProgress: (Float, Long) -> Unit
    ): Result<DownloadResult>
    
    fun cancelDownload(taskId: String)
    fun pauseDownload(taskId: String)
    fun resumeDownload(taskId: String)
}

/**
 * Result of a download operation.
 */
data class DownloadResult(
    val filePath: String,
    val fileSize: Long,
    val checksum: String
)
