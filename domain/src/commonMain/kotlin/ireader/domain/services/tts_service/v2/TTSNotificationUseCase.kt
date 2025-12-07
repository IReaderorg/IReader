package ireader.domain.services.tts_service.v2

import ireader.core.log.Log
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.tts.TTSNotificationCallback
import ireader.domain.usecases.tts.TTSNotificationManager
import ireader.domain.usecases.tts.TTSNotificationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * TTS Notification Use Case - Manages notifications for TTS v2 architecture
 * 
 * This use case:
 * - Observes TTSController state and updates notifications
 * - Handles notification action callbacks
 * - Bridges v2 architecture with existing notification system
 */
class TTSNotificationUseCase(
    private val notificationManager: TTSNotificationManager
) {
    companion object {
        private const val TAG = "TTSNotificationUseCase"
    }
    
    private var stateObserverJob: Job? = null
    private var controller: TTSController? = null
    
    /**
     * Start observing TTS state and managing notifications
     * 
     * @param controller The TTS controller to observe
     * @param scope Coroutine scope for state observation
     */
    fun start(controller: TTSController, scope: CoroutineScope) {
        Log.warn { "$TAG: start()" }
        
        this.controller = controller
        
        // Set up notification callbacks
        notificationManager.setNotificationCallback(object : TTSNotificationCallback {
            override fun onPlayPause() {
                Log.warn { "$TAG: onPlayPause()" }
                val state = controller.state.value
                if (state.isPlaying) {
                    controller.dispatch(TTSCommand.Pause)
                } else {
                    controller.dispatch(TTSCommand.Play)
                }
            }
            
            override fun onStop() {
                Log.warn { "$TAG: onStop()" }
                controller.dispatch(TTSCommand.Stop)
            }
            
            override fun onNext() {
                Log.warn { "$TAG: onNext()" }
                val state = controller.state.value
                if (state.chunkModeEnabled) {
                    controller.dispatch(TTSCommand.NextChunk)
                } else {
                    controller.dispatch(TTSCommand.NextParagraph)
                }
            }
            
            override fun onPrevious() {
                Log.warn { "$TAG: onPrevious()" }
                val state = controller.state.value
                if (state.chunkModeEnabled) {
                    controller.dispatch(TTSCommand.PreviousChunk)
                } else {
                    controller.dispatch(TTSCommand.PreviousParagraph)
                }
            }
            
            override fun onSeek(position: Int) {
                Log.warn { "$TAG: onSeek($position)" }
                controller.dispatch(TTSCommand.JumpToParagraph(position))
            }
            
            override fun onSpeedChange(speed: Float) {
                Log.warn { "$TAG: onSpeedChange($speed)" }
                controller.dispatch(TTSCommand.SetSpeed(speed))
            }
        })
        
        // Observe state changes
        stateObserverJob?.cancel()
        stateObserverJob = controller.state
            .onEach { state -> updateNotification(state) }
            .launchIn(scope)
    }
    
    /**
     * Stop observing and hide notification
     */
    fun stop() {
        Log.warn { "$TAG: stop()" }
        
        stateObserverJob?.cancel()
        stateObserverJob = null
        controller = null
        
        notificationManager.hideNotification()
    }
    
    /**
     * Update notification based on current state
     */
    private fun updateNotification(state: TTSState) {
        val book = state.book
        val chapter = state.chapter
        
        // Only show notification if we have content
        if (book == null || chapter == null || !state.hasContent) {
            if (notificationManager.isNotificationShowing()) {
                notificationManager.hideNotification()
            }
            return
        }
        
        // Hide notification if stopped
        if (state.playbackState == PlaybackState.STOPPED || 
            state.playbackState == PlaybackState.IDLE) {
            if (notificationManager.isNotificationShowing()) {
                notificationManager.hideNotification()
            }
            return
        }
        
        // Build notification state
        val notificationState = TTSNotificationState(
            isPlaying = state.isPlaying,
            isPaused = state.isPaused,
            currentParagraph = state.currentParagraphIndex,
            totalParagraphs = state.totalParagraphs,
            progress = state.progress,
            bookTitle = book.title,
            chapterTitle = chapter.name,
            bookCoverUrl = book.cover,
            speed = state.speed,
            ttsProvider = when (state.engineType) {
                EngineType.NATIVE -> "Native TTS"
                EngineType.GRADIO -> "Gradio TTS"
            }
        )
        
        // Show or update notification
        if (notificationManager.isNotificationShowing()) {
            notificationManager.updateNotification(notificationState)
        } else {
            notificationManager.showNotification(book, chapter, notificationState)
        }
    }
    
    /**
     * Manually show notification with current state
     */
    fun showNotification() {
        controller?.state?.value?.let { updateNotification(it) }
    }
    
    /**
     * Manually hide notification
     */
    fun hideNotification() {
        notificationManager.hideNotification()
    }
    
    /**
     * Check if notification is showing
     */
    fun isNotificationShowing(): Boolean {
        return notificationManager.isNotificationShowing()
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        Log.warn { "$TAG: cleanup()" }
        stop()
        notificationManager.cleanup()
    }
}
