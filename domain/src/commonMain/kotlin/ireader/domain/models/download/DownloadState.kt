package ireader.domain.models.download

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Wrapper class for Download that provides reactive state updates via StateFlow.
 * Based on Mihon's pattern for observable download state.
 */
class DownloadState(initial: Download) {
    private val _state = MutableStateFlow(initial)
    
    /**
     * Immutable StateFlow for observing download state changes.
     */
    val state: StateFlow<Download> = _state.asStateFlow()
    
    /**
     * Current download value.
     */
    val download: Download
        get() = _state.value
    
    /**
     * Chapter ID for quick identification.
     */
    val chapterId: Long
        get() = _state.value.chapterId
    
    /**
     * Book ID for quick identification.
     */
    val bookId: Long
        get() = _state.value.bookId
    
    /**
     * Source ID for quick identification.
     */
    val sourceId: Long
        get() = _state.value.sourceId
    
    /**
     * Atomically updates the download state.
     */
    fun update(transform: (Download) -> Download) {
        _state.update(transform)
    }
    
    /**
     * Sets the download state directly.
     */
    fun set(download: Download) {
        _state.value = download
    }
    
    /**
     * Updates the status.
     */
    fun updateStatus(status: DownloadStatus) {
        update { it.withStatus(status) }
    }
    
    /**
     * Updates the progress.
     */
    fun updateProgress(progress: Int, downloaded: Int = download.downloadedPages, total: Int = download.totalPages) {
        update { it.withProgress(progress, downloaded, total) }
    }
    
    /**
     * Marks the download as failed with an error message.
     */
    fun markError(message: String) {
        update { it.withError(message) }
    }
    
    /**
     * Marks the download as completed.
     */
    fun markCompleted() {
        update { it.asCompleted() }
    }
    
    /**
     * Marks the download as downloading.
     */
    fun markDownloading() {
        update { it.asDownloading() }
    }
    
    /**
     * Marks the download as queued.
     */
    fun markQueued() {
        update { it.asQueued() }
    }
    
    /**
     * Increments the retry count and requeues.
     */
    fun retry() {
        update { it.withRetry() }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DownloadState) return false
        return chapterId == other.chapterId
    }
    
    override fun hashCode(): Int = chapterId.hashCode()
    
    override fun toString(): String = "DownloadState(chapterId=$chapterId, status=${download.status})"
}
