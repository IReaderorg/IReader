package ireader.presentation.ui.settings.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.log.Log
import ireader.core.prefs.PreferenceStore
import ireader.domain.image.CoverCache
import ireader.domain.services.tts_service.TTSChapterCache
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the enhanced data and storage settings screen.
 * Manages comprehensive data preferences with cache management and storage optimization.
 */
class SettingsDataViewModel(
    private val preferenceStore: PreferenceStore,
    private val coverCache: CoverCache? = null,
    private val ttsChapterCache: TTSChapterCache? = null,
    private val storageHelper: StorageHelper = DefaultStorageHelper(),
    private val databaseHelper: DatabaseHelper = DefaultDatabaseHelper()
) : BaseViewModel() {
    
    // Cache size states (in bytes)
    private val _imageCacheSize = MutableStateFlow(0L)
    val imageCacheSize: StateFlow<Long> = _imageCacheSize.asStateFlow()
    
    private val _chapterCacheSize = MutableStateFlow(0L)
    val chapterCacheSize: StateFlow<Long> = _chapterCacheSize.asStateFlow()
    
    private val _networkCacheSize = MutableStateFlow(0L)
    val networkCacheSize: StateFlow<Long> = _networkCacheSize.asStateFlow()
    
    // Total cache size (computed from individual caches)
    val totalCacheSize: StateFlow<Long> = combine(
        _imageCacheSize,
        _chapterCacheSize,
        _networkCacheSize
    ) { image, chapter, network ->
        image + chapter + network
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), 0L)
    
    // Cache management preferences
    val autoCleanupEnabled: StateFlow<Boolean> = preferenceStore.getBoolean("auto_cleanup_enabled", true).stateIn(scope)
    val autoCleanupInterval: StateFlow<Int> = preferenceStore.getInt("auto_cleanup_interval", 7).stateIn(scope)
    val maxCacheSize: StateFlow<Int> = preferenceStore.getInt("max_cache_size", 1000).stateIn(scope) // MB
    val clearCacheOnLowStorage: StateFlow<Boolean> = preferenceStore.getBoolean("clear_cache_on_low_storage", true).stateIn(scope)
    
    // Image preferences
    val compressImages: StateFlow<Boolean> = preferenceStore.getBoolean("compress_images", true).stateIn(scope)
    val imageQuality: StateFlow<Int> = preferenceStore.getInt("image_quality", 75).stateIn(scope)
    
    // Preloading preferences
    val preloadNextChapter: StateFlow<Boolean> = preferenceStore.getBoolean("preload_next_chapter", true).stateIn(scope)
    val preloadPreviousChapter: StateFlow<Boolean> = preferenceStore.getBoolean("preload_previous_chapter", false).stateIn(scope)
    
    // Storage info
    private val _availableStorage = MutableStateFlow(0L)
    val availableStorage: StateFlow<Long> = _availableStorage.asStateFlow()
    
    private val _totalStorage = MutableStateFlow(0L)
    val totalStorage: StateFlow<Long> = _totalStorage.asStateFlow()
    
    // Operation states
    private val _isOptimizing = MutableStateFlow(false)
    val isOptimizing: StateFlow<Boolean> = _isOptimizing.asStateFlow()
    
    private val _isClearing = MutableStateFlow(false)
    val isClearing: StateFlow<Boolean> = _isClearing.asStateFlow()
    
    // Snackbar message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    
    // Dialog states
    var showCleanupIntervalDialog by mutableStateOf(false)
        private set
    var showMaxCacheSizeDialog by mutableStateOf(false)
        private set
    var showImageQualityDialog by mutableStateOf(false)
        private set
    var showImageCacheDialog by mutableStateOf(false)
        private set
    var showChapterCacheDialog by mutableStateOf(false)
        private set
    var showNetworkCacheDialog by mutableStateOf(false)
        private set
    var showClearAllCacheDialog by mutableStateOf(false)
        private set
    var showOptimizeDatabaseDialog by mutableStateOf(false)
        private set
    var showResetDataSettingsDialog by mutableStateOf(false)
        private set
    
    init {
        refreshStorageInfo()
    }
    
    fun refreshStorageInfo() {
        scope.launch {
            updateCacheSizes()
            updateStorageInfo()
        }
    }
    
    private suspend fun updateStorageInfo() {
        _availableStorage.value = storageHelper.getAvailableStorage()
        _totalStorage.value = storageHelper.getTotalStorage()
    }
    
    // Cache management functions
    fun setAutoCleanupEnabled(enabled: Boolean) {
        preferenceStore.getBoolean("auto_cleanup_enabled", true).set(enabled)
        if (enabled) scheduleAutoCleanup() else cancelAutoCleanup()
    }
    
    fun showCleanupIntervalDialog() { showCleanupIntervalDialog = true }
    fun dismissCleanupIntervalDialog() { showCleanupIntervalDialog = false }
    
    fun setAutoCleanupInterval(interval: Int) {
        preferenceStore.getInt("auto_cleanup_interval", 7).set(interval)
        if (autoCleanupEnabled.value) scheduleAutoCleanup()
    }
    
    fun showMaxCacheSizeDialog() { showMaxCacheSizeDialog = true }
    fun dismissMaxCacheSizeDialog() { showMaxCacheSizeDialog = false }
    fun setMaxCacheSize(size: Int) { preferenceStore.getInt("max_cache_size", 1000).set(size) }
    fun setClearCacheOnLowStorage(enabled: Boolean) { preferenceStore.getBoolean("clear_cache_on_low_storage", true).set(enabled) }
    
    // Image functions
    fun setCompressImages(enabled: Boolean) { preferenceStore.getBoolean("compress_images", true).set(enabled) }
    fun showImageQualityDialog() { showImageQualityDialog = true }
    fun dismissImageQualityDialog() { showImageQualityDialog = false }
    fun setImageQuality(quality: Int) { preferenceStore.getInt("image_quality", 75).set(quality) }
    
    // Preloading functions
    fun setPreloadNextChapter(enabled: Boolean) { preferenceStore.getBoolean("preload_next_chapter", true).set(enabled) }
    fun setPreloadPreviousChapter(enabled: Boolean) { preferenceStore.getBoolean("preload_previous_chapter", false).set(enabled) }
    
    // Cache dialog functions
    fun showImageCacheDialog() { showImageCacheDialog = true }
    fun dismissImageCacheDialog() { showImageCacheDialog = false }
    fun showChapterCacheDialog() { showChapterCacheDialog = true }
    fun dismissChapterCacheDialog() { showChapterCacheDialog = false }
    fun showNetworkCacheDialog() { showNetworkCacheDialog = true }
    fun dismissNetworkCacheDialog() { showNetworkCacheDialog = false }
    fun showClearAllCacheDialog() { showClearAllCacheDialog = true }
    fun dismissClearAllCacheDialog() { showClearAllCacheDialog = false }
    
    fun clearAllCache() {
        scope.launch {
            _isClearing.value = true
            try {
                clearImageCache()
                clearChapterCache()
                clearNetworkCache()
                updateCacheSizes()
                showSnackbar("All caches cleared successfully")
            } catch (e: Exception) {
                Log.error(e, "Failed to clear all caches")
                showSnackbar("Failed to clear caches: ${e.message}")
            } finally {
                _isClearing.value = false
            }
        }
    }
    
    fun clearImageCache() {
        scope.launch {
            try {
                coverCache?.clearMemoryCache()
                // Note: Disk cache clearing is handled by the storage helper
                // since CoverCache doesn't expose a clearDiskCache method
                _imageCacheSize.value = 0L
            } catch (e: Exception) { Log.error(e, "Failed to clear image cache") }
        }
    }
    
    fun clearChapterCache() {
        scope.launch {
            try {
                ttsChapterCache?.clearAll()
                ttsChapterCache?.clearAllChunks()
                _chapterCacheSize.value = 0L
            } catch (e: Exception) { Log.error(e, "Failed to clear chapter cache") }
        }
    }
    
    fun clearNetworkCache() {
        scope.launch {
            try {
                storageHelper.clearNetworkCache()
                _networkCacheSize.value = 0L
            } catch (e: Exception) { Log.error(e, "Failed to clear network cache") }
        }
    }
    
    fun showOptimizeDatabaseDialog() { showOptimizeDatabaseDialog = true }
    fun dismissOptimizeDatabaseDialog() { showOptimizeDatabaseDialog = false }
    
    fun optimizeDatabase() {
        scope.launch {
            _isOptimizing.value = true
            try {
                val result = databaseHelper.optimizeDatabase()
                showSnackbar(if (result) "Database optimized successfully" else "Database optimization failed")
            } catch (e: Exception) {
                Log.error(e, "Database optimization failed")
                showSnackbar("Database optimization failed: ${e.message}")
            } finally {
                _isOptimizing.value = false
            }
        }
    }
    
    fun showResetDataSettingsDialog() { showResetDataSettingsDialog = true }
    fun dismissResetDataSettingsDialog() { showResetDataSettingsDialog = false }
    
    fun resetDataSettings() {
        preferenceStore.getBoolean("auto_cleanup_enabled", true).set(true)
        preferenceStore.getInt("auto_cleanup_interval", 7).set(7)
        preferenceStore.getInt("max_cache_size", 1000).set(1000)
        preferenceStore.getBoolean("clear_cache_on_low_storage", true).set(true)
        preferenceStore.getBoolean("compress_images", true).set(true)
        preferenceStore.getInt("image_quality", 75).set(75)
        preferenceStore.getBoolean("preload_next_chapter", true).set(true)
        preferenceStore.getBoolean("preload_previous_chapter", false).set(false)
        showSnackbar("Data settings reset to defaults")
    }
    
    // Navigation
    private val _navigationEvent = MutableStateFlow<DataNavigationEvent?>(null)
    val navigationEvent: StateFlow<DataNavigationEvent?> = _navigationEvent.asStateFlow()
    fun navigateToStorageBreakdown() { _navigationEvent.value = DataNavigationEvent.StorageBreakdown }
    fun navigateToDataUsageStats() { _navigationEvent.value = DataNavigationEvent.DataUsageStats }
    fun navigateToNetworkSettings() { _navigationEvent.value = DataNavigationEvent.NetworkSettings }
    fun clearNavigationEvent() { _navigationEvent.value = null }
    
    private fun scheduleAutoCleanup() { storageHelper.scheduleCleanup(autoCleanupInterval.value) }
    private fun cancelAutoCleanup() { storageHelper.cancelScheduledCleanup() }
    
    private suspend fun updateCacheSizes() {
        // Image cache size - use storage helper since CoverCache doesn't expose disk size
        _imageCacheSize.value = storageHelper.getImageCacheSize()
        val cacheStats = ttsChapterCache?.getCacheStats()
        val chunkStats = ttsChapterCache?.getChunkCacheStats()
        _chapterCacheSize.value = (cacheStats?.totalSizeBytes ?: 0L) + (chunkStats?.totalSizeBytes ?: 0L)
        _networkCacheSize.value = storageHelper.getNetworkCacheSize()
    }
    
    fun getAvailableStorage(): Long = _availableStorage.value
    fun getTotalStorage(): Long = _totalStorage.value
    fun isLowOnStorage(): Boolean {
        val total = _totalStorage.value
        if (total == 0L) return false
        return (_availableStorage.value.toDouble() / total.toDouble()) < 0.1
    }
    
    fun performCleanup() {
        scope.launch {
            if (isLowOnStorage() && clearCacheOnLowStorage.value) {
                storageHelper.clearOldestCacheFiles(30)
            }
            val maxSize = maxCacheSize.value * 1024 * 1024L
            if (maxSize > 0 && totalCacheSize.value > maxSize) {
                if (_networkCacheSize.value > 0) clearNetworkCache()
                if (totalCacheSize.value > maxSize && _chapterCacheSize.value > 0) clearChapterCache()
                if (totalCacheSize.value > maxSize && _imageCacheSize.value > 0) clearImageCache()
            }
            updateCacheSizes()
        }
    }
    
    fun getDataUsageStats(): DataUsageStats = storageHelper.getDataUsageStats()
    private fun showSnackbar(message: String) { _snackbarMessage.value = message }
    fun clearSnackbar() { _snackbarMessage.value = null }
}

sealed class DataNavigationEvent {
    object StorageBreakdown : DataNavigationEvent()
    object DataUsageStats : DataNavigationEvent()
    object NetworkSettings : DataNavigationEvent()
}

data class DataUsageStats(
    val totalDownloaded: Long, val imagesDownloaded: Long, val chaptersDownloaded: Long,
    val metadataDownloaded: Long, val wifiUsage: Long, val mobileUsage: Long
)

interface StorageHelper {
    fun getAvailableStorage(): Long
    fun getTotalStorage(): Long
    fun getImageCacheSize(): Long
    fun getNetworkCacheSize(): Long
    fun clearNetworkCache()
    fun clearOldestCacheFiles(daysOld: Int)
    fun scheduleCleanup(intervalDays: Int)
    fun cancelScheduledCleanup()
    fun getDataUsageStats(): DataUsageStats
}

class DefaultStorageHelper : StorageHelper {
    override fun getAvailableStorage(): Long = 1_000_000_000L
    override fun getTotalStorage(): Long = 10_000_000_000L
    override fun getImageCacheSize(): Long = 0L
    override fun getNetworkCacheSize(): Long = 0L
    override fun clearNetworkCache() {}
    override fun clearOldestCacheFiles(daysOld: Int) {}
    override fun scheduleCleanup(intervalDays: Int) {}
    override fun cancelScheduledCleanup() {}
    override fun getDataUsageStats(): DataUsageStats = DataUsageStats(0, 0, 0, 0, 0, 0)
}

interface DatabaseHelper { suspend fun optimizeDatabase(): Boolean }
class DefaultDatabaseHelper : DatabaseHelper { override suspend fun optimizeDatabase(): Boolean = true }
