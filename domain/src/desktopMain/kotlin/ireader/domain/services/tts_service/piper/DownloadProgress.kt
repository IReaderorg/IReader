package ireader.domain.services.tts_service.piper

/**
 * Represents download progress for voice model downloads
 * This is a placeholder that will be fully implemented in task 5.4
 * 
 * @property downloaded Number of bytes downloaded
 * @property total Total number of bytes to download
 * @property status Status message describing the current download state
 */
data class DownloadProgress(
    val downloaded: Long,
    val total: Long,
    val status: String
) {
    val progress: Float
        get() = if (total > 0) downloaded.toFloat() / total.toFloat() else 0f
}
