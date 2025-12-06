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
 * Manages downloading entire chapter audio for remote TTS engines.
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
        val errorMessage: String? = null
    ) {
        val progressPercent: Int get() = 
            if (totalParagraphs > 0) ((currentParagraph.toFloat() / totalParagraphs) * 100).toInt() else 0
        val progressFraction: Float get() = 
            if (totalParagraphs > 0) currentParagraph.toFloat() / totalParagraphs else 0f
        val isActive: Boolean get() = 
            state == DownloadState.DOWNLOADING || state == DownloadState.PAUSED
    }

    
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
}
