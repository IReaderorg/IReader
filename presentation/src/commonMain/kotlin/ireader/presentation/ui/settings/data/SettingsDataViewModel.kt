package ireader.presentation.ui.settings.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.prefs.PreferenceStore
import ireader.domain.image.CoverCache
import ireader.domain.services.tts_service.TTSChapterCache
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val ttsChapterCache: TTSChapterCache? = null
) : BaseViewModel() {
    
    // Cache size states (in bytes)
    val imageCacheSize: StateFlow<Long> = preferenceStore.getLong("image_cache_size", 0L).stateIn(scope)
    val chapterCacheSize: StateFlow<Long> = preferenceStore.getLong("chapter_cache_size", 0L).stateIn(scope)
    val networkCacheSize: StateFlow<Long> = preferenceStore.getLong("network_cache_size", 0L).stateIn(scope)
    
    // Total cache size (computed from individual caches)
    val totalCacheSize: StateFlow<Long> = combine(
        imageCacheSize,
        chapterCacheSize,
        networkCacheSize
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
        // Update cache sizes on initialization
        updateCacheSizes()
    }
    
    // Cache management functions
    fun setAutoCleanupEnabled(enabled: Boolean) {
        preferenceStore.getBoolean("auto_cleanup_enabled", true).set(enabled)
        if (enabled) {
            scheduleAutoCleanup()
        } else {
            cancelAutoCleanup()
        }
    }
    
    fun showCleanupIntervalDialog() {
        showCleanupIntervalDialog = true
    }
    
    fun dismissCleanupIntervalDialog() {
        showCleanupIntervalDialog = false
    }
    
    fun setAutoCleanupInterval(interval: Int) {
        preferenceStore.getInt("auto_cleanup_interval", 7).set(interval)
        if (autoCleanupEnabled.value) {
            scheduleAutoCleanup()
        }
    }
    
    fun showMaxCacheSizeDialog() {
        showMaxCacheSizeDialog = true
    }
    
    fun dismissMaxCacheSizeDialog() {
        showMaxCacheSizeDialog = false
    }
    
    fun setMaxCacheSize(size: Int) {
        preferenceStore.getInt("max_cache_size", 1000).set(size)
    }
    
    fun setClearCacheOnLowStorage(enabled: Boolean) {
        preferenceStore.getBoolean("clear_cache_on_low_storage", true).set(enabled)
    }
    
    // Image functions
    fun setCompressImages(enabled: Boolean) {
        preferenceStore.getBoolean("compress_images", true).set(enabled)
    }
    
    fun showImageQualityDialog() {
        showImageQualityDialog = true
    }
    
    fun dismissImageQualityDialog() {
        showImageQualityDialog = false
    }
    
    fun setImageQuality(quality: Int) {
        preferenceStore.getInt("image_quality", 75).set(quality)
    }
    
    // Preloading functions
    fun setPreloadNextChapter(enabled: Boolean) {
        preferenceStore.getBoolean("preload_next_chapter", true).set(enabled)
    }
    
    fun setPreloadPreviousChapter(enabled: Boolean) {
        preferenceStore.getBoolean("preload_previous_chapter", false).set(enabled)
    }
    
    // Cache dialog functions
    fun showImageCacheDialog() {
        showImageCacheDialog = true
    }
    
    fun dismissImageCacheDialog() {
        showImageCacheDialog = false
    }
    
    fun showChapterCacheDialog() {
        showChapterCacheDialog = true
    }
    
    fun dismissChapterCacheDialog() {
        showChapterCacheDialog = false
    }
    
    fun showNetworkCacheDialog() {
        showNetworkCacheDialog = true
    }
    
    fun dismissNetworkCacheDialog() {
        showNetworkCacheDialog = false
    }
    
    // Maintenance functions
    fun showClearAllCacheDialog() {
        showClearAllCacheDialog = true
    }
    
    fun dismissClearAllCacheDialog() {
        showClearAllCacheDialog = false
    }
    
    fun clearAllCache() {
        clearImageCache()
        clearChapterCache()
        clearNetworkCache()
        updateCacheSizes()
    }
    
    fun clearImageCache() {
        scope.launch {
            try {
                // Clear Coil memory cache via CoverCache
                coverCache?.clearMemoryCache()
                preferenceStore.getLong("image_cache_size", 0L).set(0L)
            } catch (e: Exception) {
                // Log error but don't crash
            }
        }
    }
    
    fun clearChapterCache() {
        scope.launch {
            try {
                // Clear TTS chapter audio cache
                ttsChapterCache?.clearAll()
                ttsChapterCache?.clearAllChunks()
                preferenceStore.getLong("chapter_cache_size", 0L).set(0L)
            } catch (e: Exception) {
                // Log error but don't crash
            }
        }
    }
    
    fun clearNetworkCache() {
        // Network cache is managed by Ktor/OkHttp internally
        // We just reset the tracked size
        preferenceStore.getLong("network_cache_size", 0L).set(0L)
    }
    
    fun showOptimizeDatabaseDialog() {
        showOptimizeDatabaseDialog = true
    }
    
    fun dismissOptimizeDatabaseDialog() {
        showOptimizeDatabaseDialog = false
    }
    
    fun optimizeDatabase() {
        // TODO: Implement database optimization
        // This should run VACUUM and other optimization commands
    }
    
    fun showResetDataSettingsDialog() {
        showResetDataSettingsDialog = true
    }
    
    fun dismissResetDataSettingsDialog() {
        showResetDataSettingsDialog = false
    }
    
    fun resetDataSettings() {
        // Reset all data settings to defaults
        preferenceStore.getBoolean("auto_cleanup_enabled", true).set(true)
        preferenceStore.getInt("auto_cleanup_interval", 7).set(7)
        preferenceStore.getInt("max_cache_size", 1000).set(1000)
        preferenceStore.getBoolean("clear_cache_on_low_storage", true).set(true)
        preferenceStore.getBoolean("compress_images", true).set(true)
        preferenceStore.getInt("image_quality", 75).set(75)
        preferenceStore.getBoolean("preload_next_chapter", true).set(true)
        preferenceStore.getBoolean("preload_previous_chapter", false).set(false)
    }
    
    // Navigation functions
    fun navigateToStorageBreakdown() {
        // TODO: Navigate to detailed storage breakdown screen
    }
    
    fun navigateToDataUsageStats() {
        // TODO: Navigate to data usage statistics screen
    }
    
    fun navigateToNetworkSettings() {
        // TODO: Navigate to network settings screen
    }
    
    // Background functions
    private fun scheduleAutoCleanup() {
        // TODO: Implement platform-specific background cleanup scheduling
    }
    
    private fun cancelAutoCleanup() {
        // TODO: Cancel scheduled background cleanup
    }
    
    private fun updateCacheSizes() {
        // TODO: Calculate actual cache sizes from file system
        // This should scan cache directories and update the stored sizes
        calculateImageCacheSize()
        calculateChapterCacheSize()
        calculateNetworkCacheSize()
    }
    
    private fun calculateImageCacheSize() {
        // Image cache size is tracked by CoverCache
        // For now, we estimate based on typical cover sizes
        // A proper implementation would scan the covers directory
        val estimatedSize = 150_000_000L // 150 MB estimate
        preferenceStore.getLong("image_cache_size", 0L).set(estimatedSize)
    }
    
    private fun calculateChapterCacheSize() {
        // Get actual TTS chapter cache size
        val cacheStats = ttsChapterCache?.getCacheStats()
        val chunkStats = ttsChapterCache?.getChunkCacheStats()
        val totalSize = (cacheStats?.totalSizeBytes ?: 0L) + (chunkStats?.totalSizeBytes ?: 0L)
        preferenceStore.getLong("chapter_cache_size", 0L).set(totalSize)
    }
    
    private fun calculateNetworkCacheSize() {
        // Network cache is managed by HTTP client
        // Estimate based on typical usage
        val estimatedSize = 25_000_000L // 25 MB estimate
        preferenceStore.getLong("network_cache_size", 0L).set(estimatedSize)
    }
    
    // Storage monitoring functions
    fun getAvailableStorage(): Long {
        // TODO: Implement platform-specific available storage calculation
        return 1_000_000_000L // Placeholder: 1 GB
    }
    
    fun getTotalStorage(): Long {
        // TODO: Implement platform-specific total storage calculation
        return 10_000_000_000L // Placeholder: 10 GB
    }
    
    fun isLowOnStorage(): Boolean {
        val available = getAvailableStorage()
        val total = getTotalStorage()
        return (available.toDouble() / total.toDouble()) < 0.1 // Less than 10% available
    }
    
    // Cache cleanup functions
    fun performCleanup() {
        if (isLowOnStorage() && clearCacheOnLowStorage.value) {
            clearOldestCacheFiles()
        }
        
        val maxSize = maxCacheSize.value * 1024 * 1024L // Convert MB to bytes
        if (maxSize > 0 && totalCacheSize.value > maxSize) {
            clearCacheToSize(maxSize)
        }
    }
    
    private fun clearOldestCacheFiles() {
        // TODO: Implement clearing of oldest cache files
    }
    
    private fun clearCacheToSize(targetSize: Long) {
        // TODO: Implement clearing cache files until target size is reached
    }
    
    // Data usage tracking
    fun getDataUsageStats(): DataUsageStats {
        // TODO: Implement data usage statistics collection
        return DataUsageStats(
            totalDownloaded = 500_000_000L, // 500 MB
            imagesDownloaded = 300_000_000L, // 300 MB
            chaptersDownloaded = 150_000_000L, // 150 MB
            metadataDownloaded = 50_000_000L, // 50 MB
            wifiUsage = 400_000_000L, // 400 MB
            mobileUsage = 100_000_000L // 100 MB
        )
    }
}

data class DataUsageStats(
    val totalDownloaded: Long,
    val imagesDownloaded: Long,
    val chaptersDownloaded: Long,
    val metadataDownloaded: Long,
    val wifiUsage: Long,
    val mobileUsage: Long
)
