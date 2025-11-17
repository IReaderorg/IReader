package ireader.presentation.ui.download

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import ireader.domain.data.repository.DownloadRepository
import ireader.domain.models.download.*
import ireader.domain.usecases.download.DownloadManagerUseCase
import ireader.domain.usecases.download.DownloadCacheUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Screen model for download queue management following Mihon's pattern
 */
class DownloadQueueScreenModel(
    private val downloadRepository: DownloadRepository,
    private val downloadManagerUseCase: DownloadManagerUseCase
) : StateScreenModel<DownloadQueueScreenModel.State>(State()) {
    
    data class State(
        val downloadQueue: List<DownloadItem> = emptyList(),
        val downloadStats: DownloadStats = DownloadStats(),
        val selectedDownloads: Set<Long> = emptySet(),
        val filterStatus: DownloadStatus? = null,
        val sortOrder: DownloadSortOrder = DownloadSortOrder.PRIORITY,
        val isLoading: Boolean = true,
        val showCompleted: Boolean = false,
        val showFailed: Boolean = true
    )
    
    enum class DownloadSortOrder {
        PRIORITY, BOOK_TITLE, CHAPTER_TITLE, DATE_ADDED, STATUS
    }
    
    init {
        observeDownloadQueue()
        observeDownloadStats()
    }
    
    private fun observeDownloadQueue() {
        downloadManagerUseCase.getDownloadQueue()
            .onEach { downloads ->
                mutableState.value = mutableState.value.copy(
                    downloadQueue = downloads,
                    isLoading = false
                )
            }
            .launchIn(screenModelScope)
    }
    
    private fun observeDownloadStats() {
        screenModelScope.launch {
            try {
                val stats = downloadManagerUseCase.getDownloadStats()
                mutableState.value = mutableState.value.copy(
                    downloadStats = stats
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun selectDownload(chapterId: Long) {
        val currentSelected = mutableState.value.selectedDownloads
        mutableState.value = mutableState.value.copy(
            selectedDownloads = if (chapterId in currentSelected) {
                currentSelected - chapterId
            } else {
                currentSelected + chapterId
            }
        )
    }
    
    fun selectAllDownloads() {
        val filteredDownloads = getFilteredDownloads()
        mutableState.value = mutableState.value.copy(
            selectedDownloads = filteredDownloads.map { it.chapterId }.toSet()
        )
    }
    
    fun clearSelection() {
        mutableState.value = mutableState.value.copy(
            selectedDownloads = emptySet()
        )
    }
    
    fun pauseDownload(chapterId: Long) {
        screenModelScope.launch {
            downloadManagerUseCase.pauseDownload(chapterId)
        }
    }
    
    fun resumeDownload(chapterId: Long) {
        screenModelScope.launch {
            downloadManagerUseCase.resumeDownload(chapterId)
        }
    }
    
    fun cancelDownload(chapterId: Long) {
        screenModelScope.launch {
            downloadManagerUseCase.cancelDownload(chapterId)
        }
    }
    
    fun retryDownload(chapterId: Long) {
        screenModelScope.launch {
            downloadManagerUseCase.retryDownload(chapterId)
        }
    }
    
    fun pauseAllDownloads() {
        screenModelScope.launch {
            downloadRepository.pauseAllDownloads()
        }
    }
    
    fun resumeAllDownloads() {
        screenModelScope.launch {
            downloadRepository.resumeAllDownloads()
        }
    }
    
    fun cancelAllDownloads() {
        screenModelScope.launch {
            downloadRepository.cancelAllDownloads()
        }
    }
    
    fun cancelSelectedDownloads() {
        screenModelScope.launch {
            mutableState.value.selectedDownloads.forEach { chapterId ->
                downloadManagerUseCase.cancelDownload(chapterId)
            }
            clearSelection()
        }
    }
    
    fun retrySelectedDownloads() {
        screenModelScope.launch {
            mutableState.value.selectedDownloads.forEach { chapterId ->
                downloadManagerUseCase.retryDownload(chapterId)
            }
            clearSelection()
        }
    }
    
    fun clearCompleted() {
        screenModelScope.launch {
            downloadManagerUseCase.clearCompleted()
        }
    }
    
    fun clearFailed() {
        screenModelScope.launch {
            downloadManagerUseCase.clearFailed()
        }
    }
    
    fun reorderQueue(chapterIds: List<Long>) {
        screenModelScope.launch {
            downloadManagerUseCase.reorderQueue(chapterIds)
        }
    }
    
    fun updateFilterStatus(status: DownloadStatus?) {
        mutableState.value = mutableState.value.copy(
            filterStatus = status
        )
    }
    
    fun updateSortOrder(order: DownloadSortOrder) {
        mutableState.value = mutableState.value.copy(
            sortOrder = order
        )
    }
    
    fun toggleShowCompleted() {
        mutableState.value = mutableState.value.copy(
            showCompleted = !mutableState.value.showCompleted
        )
    }
    
    fun toggleShowFailed() {
        mutableState.value = mutableState.value.copy(
            showFailed = !mutableState.value.showFailed
        )
    }
    
    fun getFilteredDownloads(): List<DownloadItem> {
        val state = mutableState.value
        var downloads = state.downloadQueue
        
        // Apply status filter
        state.filterStatus?.let { status ->
            downloads = downloads.filter { it.status == status }
        }
        
        // Apply visibility filters
        if (!state.showCompleted) {
            downloads = downloads.filter { it.status != DownloadStatus.COMPLETED }
        }
        
        if (!state.showFailed) {
            downloads = downloads.filter { it.status != DownloadStatus.FAILED }
        }
        
        // Apply sort order
        downloads = when (state.sortOrder) {
            DownloadSortOrder.PRIORITY -> downloads.sortedByDescending { it.priority }
            DownloadSortOrder.BOOK_TITLE -> downloads.sortedBy { it.bookTitle }
            DownloadSortOrder.CHAPTER_TITLE -> downloads.sortedBy { it.chapterTitle }
            DownloadSortOrder.DATE_ADDED -> downloads.sortedByDescending { it.createdAt }
            DownloadSortOrder.STATUS -> downloads.sortedBy { it.status.ordinal }
        }
        
        return downloads
    }
}

/**
 * Screen model for download settings and configuration
 */
class DownloadSettingsScreenModel(
    private val downloadRepository: DownloadRepository,
    private val downloadCacheUseCase: DownloadCacheUseCase
) : StateScreenModel<DownloadSettingsScreenModel.State>(State()) {
    
    data class State(
        val config: DownloadQueueConfig = DownloadQueueConfig(),
        val cacheSize: Long = 0L,
        val cacheEntries: Int = 0,
        val isLoading: Boolean = true
    )
    
    init {
        loadConfiguration()
        loadCacheInfo()
    }
    
    private fun loadConfiguration() {
        screenModelScope.launch {
            try {
                val config = downloadRepository.getDownloadQueueConfig()
                mutableState.value = mutableState.value.copy(
                    config = config,
                    isLoading = false
                )
            } catch (e: Exception) {
                mutableState.value = mutableState.value.copy(
                    isLoading = false
                )
            }
        }
    }
    
    private fun loadCacheInfo() {
        screenModelScope.launch {
            try {
                val cacheSize = downloadCacheUseCase.getCacheSize()
                val cacheEntries = downloadRepository.getDownloadCacheEntries()
                
                mutableState.value = mutableState.value.copy(
                    cacheSize = cacheSize,
                    cacheEntries = cacheEntries.size
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun updateConfig(config: DownloadQueueConfig) {
        mutableState.value = mutableState.value.copy(config = config)
        
        screenModelScope.launch {
            downloadRepository.saveDownloadQueueConfig(config)
        }
    }
    
    fun clearCache() {
        screenModelScope.launch {
            downloadCacheUseCase.clearAllCache()
            loadCacheInfo()
        }
    }
    
    fun cleanupOldCache() {
        screenModelScope.launch {
            downloadCacheUseCase.cleanupOldEntries()
            loadCacheInfo()
        }
    }
    
    fun cleanupInvalidCache() {
        screenModelScope.launch {
            downloadCacheUseCase.cleanupInvalidEntries()
            loadCacheInfo()
        }
    }
    
    fun formatCacheSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}