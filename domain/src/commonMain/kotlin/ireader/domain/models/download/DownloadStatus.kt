package ireader.domain.models.download

/**
 * Represents the status of a download.
 * Based on Mihon's download status enum for consistent state management.
 */
enum class DownloadStatus {
    /**
     * Download has not been started.
     */
    NOT_DOWNLOADED,
    
    /**
     * Download is waiting in the queue.
     */
    QUEUE,
    
    /**
     * Download is waiting in the queue (alias for QUEUE).
     */
    QUEUED,
    
    /**
     * Download is currently in progress.
     */
    DOWNLOADING,
    
    /**
     * Download has completed successfully.
     */
    DOWNLOADED,
    
    /**
     * Download has completed successfully (alias for DOWNLOADED).
     */
    COMPLETED,
    
    /**
     * Download failed with an error.
     */
    ERROR,
    
    /**
     * Download failed with an error (alias for ERROR).
     */
    FAILED,
    
    /**
     * Download is paused.
     */
    PAUSED,
    
    /**
     * Download was cancelled.
     */
    CANCELLED;
    
    /**
     * Returns true if this status represents a pending state (queued or downloading).
     */
    val isPending: Boolean
        get() = this == QUEUE || this == QUEUED || this == DOWNLOADING
    
    /**
     * Returns true if this status allows retry.
     */
    val canRetry: Boolean
        get() = this == ERROR || this == FAILED || this == NOT_DOWNLOADED || this == CANCELLED
    
    /**
     * Returns true if this status allows cancellation.
     */
    val canCancel: Boolean
        get() = this == QUEUE || this == QUEUED || this == DOWNLOADING || this == ERROR || this == FAILED || this == PAUSED
    
    /**
     * Returns true if this status represents a terminal state.
     */
    val isTerminal: Boolean
        get() = this == DOWNLOADED || this == COMPLETED || this == ERROR || this == FAILED || this == CANCELLED
    
    /**
     * Returns true if this status represents an active download.
     */
    val isActive: Boolean
        get() = this == DOWNLOADING
    
    /**
     * Returns true if this status is paused.
     */
    val isPaused: Boolean
        get() = this == PAUSED
    
    /**
     * Returns true if this status represents a completed download.
     */
    val isCompleted: Boolean
        get() = this == DOWNLOADED || this == COMPLETED
    
    /**
     * Returns true if this status represents a failed download.
     */
    val isFailed: Boolean
        get() = this == ERROR || this == FAILED
    
    companion object {
        /**
         * Returns the status from its ordinal value.
         */
        fun fromOrdinal(ordinal: Int): DownloadStatus =
            values().getOrElse(ordinal) { NOT_DOWNLOADED }
    }
}
