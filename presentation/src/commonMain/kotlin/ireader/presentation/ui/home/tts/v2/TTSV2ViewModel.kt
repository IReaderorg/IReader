package ireader.presentation.ui.home.tts.v2

import ireader.domain.services.tts_service.v2.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * TTS V2 ViewModel - Example ViewModel using the new TTS v2 architecture
 * 
 * This ViewModel demonstrates how to:
 * - Create and manage TTSViewModelAdapter
 * - Initialize TTS controller
 * - Load chapter content
 * - Use sleep timer and notifications
 * - Handle lifecycle
 * 
 * In a real implementation, this would extend your platform's ViewModel class
 * (e.g., AndroidX ViewModel) and use viewModelScope.
 */
class TTSV2ViewModel(
    private val controller: TTSController,
    private val sleepTimerUseCase: TTSSleepTimerUseCase? = null,
    private val notificationUseCase: TTSNotificationUseCase? = null,
    private val preferencesUseCase: TTSPreferencesUseCase? = null
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Create adapter with the controller
    val adapter = TTSViewModelAdapter(controller, scope)
    
    // Sleep timer state for UI
    val sleepTimerState = sleepTimerUseCase?.state
    
    init {
        // Initialize the TTS engine
        adapter.initialize()
        
        // Initialize preferences (loads saved settings)
        preferencesUseCase?.initialize(controller, scope)
        
        // Initialize sleep timer
        sleepTimerUseCase?.initialize(controller, scope)
        
        // Start notification management
        notificationUseCase?.start(controller, scope)
    }
    
    /**
     * Load a chapter for TTS playback
     */
    fun loadChapter(bookId: Long, chapterId: Long, startParagraph: Int = 0) {
        adapter.loadChapter(bookId, chapterId, startParagraph)
    }
    
    /**
     * Start playback with Gradio TTS and chunk mode
     * 
     * @param config Gradio TTS configuration
     * @param targetWordCount Words per chunk for text merging
     */
    fun startWithGradioTTS(config: GradioConfig, targetWordCount: Int = 50) {
        adapter.useGradioTTSWithChunks(config, targetWordCount)
    }
    
    /**
     * Switch to native TTS
     */
    fun useNativeTTS() {
        adapter.useNativeTTS()
        adapter.disableChunkMode()
    }
    
    // ========== Sleep Timer ==========
    
    /**
     * Start sleep timer
     * @param minutes Duration in minutes
     */
    fun startSleepTimer(minutes: Int) {
        sleepTimerUseCase?.start(minutes)
    }
    
    /**
     * Add time to existing sleep timer
     * @param minutes Additional minutes
     */
    fun addSleepTimerTime(minutes: Int) {
        sleepTimerUseCase?.addTime(minutes)
    }
    
    /**
     * Cancel sleep timer
     */
    fun cancelSleepTimer() {
        sleepTimerUseCase?.cancel()
    }
    
    /**
     * Check if sleep timer is active
     */
    fun isSleepTimerActive(): Boolean {
        return sleepTimerUseCase?.isActive() == true
    }
    
    // ========== Notifications ==========
    
    /**
     * Show notification manually
     */
    fun showNotification() {
        notificationUseCase?.showNotification()
    }
    
    /**
     * Hide notification manually
     */
    fun hideNotification() {
        notificationUseCase?.hideNotification()
    }
    
    // ========== Preferences ==========
    
    /**
     * Get merge words setting for chunk mode
     */
    fun getMergeWordsRemote(): Int {
        return preferencesUseCase?.getMergeWordsRemote() ?: 0
    }
    
    /**
     * Check if chunk mode should be enabled based on preferences
     */
    fun shouldEnableChunkMode(): Boolean {
        val mergeWords = getMergeWordsRemote()
        return mergeWords > 0
    }
    
    /**
     * Enable chunk mode with saved preference
     */
    fun enableChunkModeFromPreferences() {
        val mergeWords = getMergeWordsRemote()
        if (mergeWords > 0) {
            adapter.enableChunkMode(mergeWords)
        }
    }
    
    /**
     * Clean up ViewModel resources (NOT the controller)
     * The controller state should persist while the service is running.
     * Call destroyController() only when you want to fully stop TTS.
     */
    fun onCleared() {
        preferencesUseCase?.cleanup()
        sleepTimerUseCase?.cleanup()
        notificationUseCase?.cleanup()
        // Don't call adapter.cleanup() - it would reset the controller state
        // The controller is managed by the service lifecycle
        scope.cancel()
    }
    
    /**
     * Fully destroy the controller and release all resources.
     * Only call this when you want to completely stop TTS playback.
     */
    fun destroyController() {
        adapter.cleanup()
    }
}

/**
 * Factory for creating TTSV2ViewModel
 * 
 * In a real implementation, this would be a ViewModelProvider.Factory
 * or use Koin's viewModel() function.
 */
class TTSV2ViewModelFactory(
    private val controller: TTSController,
    private val sleepTimerUseCase: TTSSleepTimerUseCase? = null,
    private val notificationUseCase: TTSNotificationUseCase? = null,
    private val preferencesUseCase: TTSPreferencesUseCase? = null
) {
    fun create(): TTSV2ViewModel {
        return TTSV2ViewModel(controller, sleepTimerUseCase, notificationUseCase, preferencesUseCase)
    }
}
