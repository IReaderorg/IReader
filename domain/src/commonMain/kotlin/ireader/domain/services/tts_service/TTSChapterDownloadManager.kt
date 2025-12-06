package ireader.domain.services.tts_service

import ireader.core.log.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * TTS Chapter Download Manager
 * Manages downloading chapter audio for remote TTS engines.
 * Supports both paragraph-by-paragraph and chunk-based downloading (text merging).
 * Shows progress notifications with pause/cancel buttons.
 */
class TTSChapterDownloadManager(
    private val notificationHelper: TTSDownloadNotificationHelper? = null
) {
    
    companion object {
        private const val TAG = "TTSChapterDownload"
    }
    
    enum class DownloadState {
        IDLE, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED
    }
    
    data class DownloadProgress(
        val chapterId: Long,
        val chapterName: String,
        val bookTitle: String,
        val currentParagraph: Int,
        val totalParagraphs: Int,
        val state: DownloadState,
        val errorMessage: String? = null,
        // For chunk-based download
        val currentChunk: Int = 0,
        val totalChunks: Int = 0
    ) {
        val progressPercent: Int get() = 
            if (totalParagraphs > 0) ((currentParagraph.toFloat() / totalParagraphs) * 100).toInt() else 0
        val progressFraction: Float get() = 
            if (totalParagraphs > 0) currentParagraph.toFloat() / totalParagraphs else 0f
        val isActive: Boolean get() = 
            state == DownloadState.DOWNLOADING || state == DownloadState.PAUSED
    }
    
    /**
     * Chunk info for download
     */
    data class ChunkInfo(
        val index: Int,
        val text: String,
        val paragraphIndices: List<Int>
    )

    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var downloadJob: Job? = null
    
    private val _progress = MutableStateFlow<DownloadProgress?>(null)
    val progress: StateFlow<DownloadProgress?> = _progress.asStateFlow()
    
    private val _state = MutableStateFlow(DownloadState.IDLE)
    val state: StateFlow<DownloadState> = _state.asStateFlow()
    
    @Volatile
    private var isPaused = false
    
    // Platform-specific intents for notification actions (set by platform code)
    var pauseIntent: Any? = null
    var cancelIntent: Any? = null
    
    fun startDownload(
        chapterId: Long,
        chapterName: String,
        bookTitle: String,
        paragraphs: List<String>,
        generateAudio: suspend (String, Int) -> ByteArray?,
        onComplete: suspend (List<ByteArray>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_state.value == DownloadState.DOWNLOADING) {
            Log.warn { "$TAG: Download already in progress" }
            return
        }
        
        Log.info { "$TAG: Starting download - $chapterName (${paragraphs.size} paragraphs)" }
        
        isPaused = false
        _state.value = DownloadState.DOWNLOADING
        _progress.value = DownloadProgress(
            chapterId = chapterId,
            chapterName = chapterName,
            bookTitle = bookTitle,
            currentParagraph = 0,
            totalParagraphs = paragraphs.size,
            state = DownloadState.DOWNLOADING
        )
        
        // Show starting notification
        notificationHelper?.showStarting(chapterName, bookTitle)
        
        downloadJob = scope.launch {
            val audioChunks = mutableListOf<ByteArray>()
            
            try {
                for ((index, paragraph) in paragraphs.withIndex()) {
                    if (_state.value == DownloadState.CANCELLED) {
                        Log.info { "$TAG: Cancelled at paragraph $index" }
                        notificationHelper?.showCancelled(chapterName)
                        return@launch
                    }
                    
                    while (isPaused && _state.value != DownloadState.CANCELLED) {
                        kotlinx.coroutines.delay(100)
                    }
                    
                    if (_state.value == DownloadState.CANCELLED) {
                        notificationHelper?.showCancelled(chapterName)
                        return@launch
                    }
                    
                    Log.debug { "$TAG: Generating audio ${index + 1}/${paragraphs.size}" }
                    
                    val currentProgress = DownloadProgress(
                        chapterId = chapterId,
                        chapterName = chapterName,
                        bookTitle = bookTitle,
                        currentParagraph = index + 1,
                        totalParagraphs = paragraphs.size,
                        state = if (isPaused) DownloadState.PAUSED else DownloadState.DOWNLOADING
                    )
                    _progress.value = currentProgress
                    
                    // Update notification with progress
                    notificationHelper?.updateProgress(
                        chapterName = chapterName,
                        bookTitle = bookTitle,
                        currentParagraph = index + 1,
                        totalParagraphs = paragraphs.size,
                        isPaused = isPaused,
                        pauseIntent = pauseIntent,
                        cancelIntent = cancelIntent
                    )
                    
                    val audio = generateAudio(paragraph, index)
                    if (audio != null) {
                        audioChunks.add(audio)
                    } else {
                        Log.warn { "$TAG: Failed to generate audio for paragraph ${index + 1}" }
                    }
                }
                
                Log.info { "$TAG: Download complete - ${audioChunks.size} chunks" }
                _state.value = DownloadState.COMPLETED
                _progress.value = _progress.value?.copy(
                    currentParagraph = paragraphs.size,
                    state = DownloadState.COMPLETED
                )
                
                // Show completion notification
                notificationHelper?.showComplete(chapterName, bookTitle)
                
                onComplete(audioChunks)
                
            } catch (e: CancellationException) {
                Log.info { "$TAG: Download cancelled" }
                _state.value = DownloadState.CANCELLED
                _progress.value = _progress.value?.copy(state = DownloadState.CANCELLED)
                notificationHelper?.showCancelled(chapterName)
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error"
                Log.error { "$TAG: Download failed: $errorMsg" }
                _state.value = DownloadState.FAILED
                _progress.value = _progress.value?.copy(
                    state = DownloadState.FAILED,
                    errorMessage = errorMsg
                )
                
                // Show failure notification
                notificationHelper?.showFailed(chapterName, bookTitle, errorMsg)
                
                onError(errorMsg)
            }
        }
    }
    
    fun pause() {
        if (_state.value == DownloadState.DOWNLOADING) {
            Log.info { "$TAG: Pausing download" }
            isPaused = true
            _state.value = DownloadState.PAUSED
            val currentProgress = _progress.value
            _progress.value = currentProgress?.copy(state = DownloadState.PAUSED)
            
            // Update notification to show paused state
            currentProgress?.let {
                notificationHelper?.updateProgress(
                    chapterName = it.chapterName,
                    bookTitle = it.bookTitle,
                    currentParagraph = it.currentParagraph,
                    totalParagraphs = it.totalParagraphs,
                    isPaused = true,
                    pauseIntent = pauseIntent,
                    cancelIntent = cancelIntent
                )
            }
        }
    }
    
    fun resume() {
        if (_state.value == DownloadState.PAUSED) {
            Log.info { "$TAG: Resuming download" }
            isPaused = false
            _state.value = DownloadState.DOWNLOADING
            val currentProgress = _progress.value
            _progress.value = currentProgress?.copy(state = DownloadState.DOWNLOADING)
            
            // Update notification to show resumed state
            currentProgress?.let {
                notificationHelper?.updateProgress(
                    chapterName = it.chapterName,
                    bookTitle = it.bookTitle,
                    currentParagraph = it.currentParagraph,
                    totalParagraphs = it.totalParagraphs,
                    isPaused = false,
                    pauseIntent = pauseIntent,
                    cancelIntent = cancelIntent
                )
            }
        }
    }
    
    fun cancel() {
        Log.info { "$TAG: Cancelling download" }
        val chapterName = _progress.value?.chapterName ?: ""
        _state.value = DownloadState.CANCELLED
        isPaused = false
        downloadJob?.cancel()
        downloadJob = null
        _progress.value = _progress.value?.copy(state = DownloadState.CANCELLED)
        
        // Show cancelled notification
        if (chapterName.isNotEmpty()) {
            notificationHelper?.showCancelled(chapterName)
        } else {
            notificationHelper?.cancel()
        }
    }
    
    fun reset() {
        cancel()
        _state.value = DownloadState.IDLE
        _progress.value = null
        notificationHelper?.cancel()
    }
    
    fun isActive(): Boolean = _state.value == DownloadState.DOWNLOADING || _state.value == DownloadState.PAUSED
    
    /**
     * Start chunk-based download (respects text merging settings)
     * Each chunk may contain multiple paragraphs merged together.
     * 
     * @param chapterId Chapter ID
     * @param chapterName Chapter name for notifications
     * @param bookTitle Book title for notifications
     * @param chunks List of chunks to download (each chunk has text and paragraph indices)
     * @param generateAudio Function to generate audio for a chunk
     * @param onChunkComplete Callback when each chunk is downloaded (for incremental caching)
     * @param onComplete Callback when all chunks are downloaded
     * @param onError Callback on error
     */
    fun startChunkDownload(
        chapterId: Long,
        chapterName: String,
        bookTitle: String,
        chunks: List<ChunkInfo>,
        generateAudio: suspend (String, Int) -> ByteArray?,
        onChunkComplete: suspend (chunkIndex: Int, audioData: ByteArray, paragraphIndices: List<Int>) -> Unit,
        onComplete: suspend () -> Unit,
        onError: (String) -> Unit
    ) {
        if (_state.value == DownloadState.DOWNLOADING) {
            Log.warn { "$TAG: Download already in progress" }
            return
        }
        
        // Calculate total paragraphs for progress display
        val totalParagraphs = chunks.flatMap { it.paragraphIndices }.distinct().size
        
        Log.info { "$TAG: Starting chunk download - $chapterName (${chunks.size} chunks, $totalParagraphs paragraphs)" }
        
        isPaused = false
        _state.value = DownloadState.DOWNLOADING
        _progress.value = DownloadProgress(
            chapterId = chapterId,
            chapterName = chapterName,
            bookTitle = bookTitle,
            currentParagraph = 0,
            totalParagraphs = totalParagraphs,
            state = DownloadState.DOWNLOADING,
            currentChunk = 0,
            totalChunks = chunks.size
        )
        
        // Show starting notification
        notificationHelper?.showStarting(chapterName, bookTitle)
        
        downloadJob = scope.launch {
            var processedParagraphs = 0
            var successfulChunks = 0
            var failedChunks = 0
            
            try {
                for ((index, chunk) in chunks.withIndex()) {
                    if (_state.value == DownloadState.CANCELLED) {
                        Log.info { "$TAG: Cancelled at chunk $index" }
                        notificationHelper?.showCancelled(chapterName)
                        return@launch
                    }
                    
                    while (isPaused && _state.value != DownloadState.CANCELLED) {
                        kotlinx.coroutines.delay(100)
                    }
                    
                    if (_state.value == DownloadState.CANCELLED) {
                        notificationHelper?.showCancelled(chapterName)
                        return@launch
                    }
                    
                    Log.debug { "$TAG: Generating audio for chunk ${index + 1}/${chunks.size} (paragraphs: ${chunk.paragraphIndices})" }
                    
                    processedParagraphs += chunk.paragraphIndices.size
                    
                    val currentProgress = DownloadProgress(
                        chapterId = chapterId,
                        chapterName = chapterName,
                        bookTitle = bookTitle,
                        currentParagraph = processedParagraphs,
                        totalParagraphs = totalParagraphs,
                        state = if (isPaused) DownloadState.PAUSED else DownloadState.DOWNLOADING,
                        currentChunk = index + 1,
                        totalChunks = chunks.size
                    )
                    _progress.value = currentProgress
                    
                    // Update notification with progress
                    notificationHelper?.updateProgress(
                        chapterName = chapterName,
                        bookTitle = bookTitle,
                        currentParagraph = processedParagraphs,
                        totalParagraphs = totalParagraphs,
                        isPaused = isPaused,
                        pauseIntent = pauseIntent,
                        cancelIntent = cancelIntent
                    )
                    
                    val audio = generateAudio(chunk.text, chunk.index)
                    if (audio != null) {
                        // Notify about completed chunk for incremental caching
                        onChunkComplete(chunk.index, audio, chunk.paragraphIndices)
                        successfulChunks++
                    } else {
                        failedChunks++
                        Log.warn { "$TAG: Failed to generate audio for chunk ${index + 1}" }
                    }
                }
                
                Log.info { "$TAG: Chunk download complete - $successfulChunks/${chunks.size} chunks succeeded, $failedChunks failed" }
                
                if (failedChunks > 0 && successfulChunks == 0) {
                    // All chunks failed
                    _state.value = DownloadState.FAILED
                    _progress.value = _progress.value?.copy(
                        state = DownloadState.FAILED,
                        errorMessage = "All $failedChunks chunks failed to download"
                    )
                    notificationHelper?.showFailed(chapterName, bookTitle, "All chunks failed")
                    onError("All $failedChunks chunks failed to download")
                } else if (failedChunks > 0) {
                    // Some chunks failed - still mark as completed but with warning
                    _state.value = DownloadState.COMPLETED
                    _progress.value = _progress.value?.copy(
                        currentParagraph = totalParagraphs,
                        state = DownloadState.COMPLETED,
                        currentChunk = chunks.size,
                        errorMessage = "$failedChunks chunks failed"
                    )
                    notificationHelper?.showComplete(chapterName, bookTitle)
                    Log.warn { "$TAG: Download completed with $failedChunks failed chunks" }
                } else {
                    // All chunks succeeded
                    _state.value = DownloadState.COMPLETED
                    _progress.value = _progress.value?.copy(
                        currentParagraph = totalParagraphs,
                        state = DownloadState.COMPLETED,
                        currentChunk = chunks.size
                    )
                    notificationHelper?.showComplete(chapterName, bookTitle)
                }
                
                // Only call onComplete if at least some chunks succeeded
                if (successfulChunks > 0) {
                    onComplete()
                }
                
            } catch (e: CancellationException) {
                Log.info { "$TAG: Chunk download cancelled" }
                _state.value = DownloadState.CANCELLED
                _progress.value = _progress.value?.copy(state = DownloadState.CANCELLED)
                notificationHelper?.showCancelled(chapterName)
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error"
                Log.error { "$TAG: Chunk download failed: $errorMsg" }
                _state.value = DownloadState.FAILED
                _progress.value = _progress.value?.copy(
                    state = DownloadState.FAILED,
                    errorMessage = errorMsg
                )
                
                // Show failure notification
                notificationHelper?.showFailed(chapterName, bookTitle, errorMsg)
                
                onError(errorMsg)
            }
        }
    }
}
