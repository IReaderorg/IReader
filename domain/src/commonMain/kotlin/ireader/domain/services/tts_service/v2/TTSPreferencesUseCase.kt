package ireader.domain.services.tts_service.v2

import ireader.core.log.Log
import ireader.domain.preferences.prefs.ReaderPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * TTS Preferences Use Case - Bridges v2 architecture with existing preferences
 * 
 * This use case:
 * - Loads saved preferences and applies them to TTSController
 * - Observes preference changes and updates controller
 * - Saves preference changes from controller to storage
 */
class TTSPreferencesUseCase(
    private val readerPreferences: ReaderPreferences
) {
    companion object {
        private const val TAG = "TTSPreferencesUseCase"
    }
    
    private var controller: TTSController? = null
    private var scope: CoroutineScope? = null
    private var observerJob: Job? = null
    
    /**
     * Initialize with controller and scope
     * Loads saved preferences and starts observing changes
     */
    fun initialize(controller: TTSController, scope: CoroutineScope) {
        Log.warn { "$TAG: initialize()" }
        
        this.controller = controller
        this.scope = scope
        
        // Load saved preferences
        loadPreferences()
        
        // Observe controller state changes to save preferences
        observeStateChanges()
    }
    
    /**
     * Load saved preferences and apply to controller
     */
    private fun loadPreferences() {
        val ctrl = controller ?: return
        
        scope?.launch {
            // Load TTS settings
            val speed = readerPreferences.ttsSpeed().get()
            val pitch = readerPreferences.ttsPitch().get()
            val autoNextChapter = readerPreferences.ttsAutoPlay().get()
            
            Log.warn { "$TAG: Loading preferences - speed=$speed, pitch=$pitch, autoNext=$autoNextChapter" }
            
            // Apply to controller
            ctrl.dispatch(TTSCommand.SetSpeed(speed))
            ctrl.dispatch(TTSCommand.SetPitch(pitch))
            ctrl.dispatch(TTSCommand.SetAutoNextChapter(autoNextChapter))
            
            // Load chunk mode settings
            val mergeWordsRemote = readerPreferences.ttsMergeWordsRemote().get()
            if (mergeWordsRemote > 0) {
                Log.warn { "$TAG: Chunk mode enabled with $mergeWordsRemote words" }
                // Note: Chunk mode is enabled when using Gradio TTS
            }
        }
    }
    
    /**
     * Observe controller state changes and save to preferences
     */
    private fun observeStateChanges() {
        val ctrl = controller ?: return
        val sc = scope ?: return
        
        observerJob?.cancel()
        observerJob = ctrl.state
            .onEach { state ->
                // Save speed if changed
                val savedSpeed = readerPreferences.ttsSpeed().get()
                if (state.speed != savedSpeed) {
                    Log.warn { "$TAG: Saving speed: ${state.speed}" }
                    readerPreferences.ttsSpeed().set(state.speed)
                }
                
                // Save pitch if changed
                val savedPitch = readerPreferences.ttsPitch().get()
                if (state.pitch != savedPitch) {
                    Log.warn { "$TAG: Saving pitch: ${state.pitch}" }
                    readerPreferences.ttsPitch().set(state.pitch)
                }
                
                // Save auto next chapter if changed
                val savedAutoNext = readerPreferences.ttsAutoPlay().get()
                if (state.autoNextChapter != savedAutoNext) {
                    Log.warn { "$TAG: Saving autoNextChapter: ${state.autoNextChapter}" }
                    readerPreferences.ttsAutoPlay().set(state.autoNextChapter)
                }
            }
            .launchIn(sc)
    }
    
    // ========== Preference Getters ==========
    
    /**
     * Get saved TTS speed
     */
    fun getSpeed(): Float = readerPreferences.ttsSpeed().get()
    
    /**
     * Get saved TTS pitch
     */
    fun getPitch(): Float = readerPreferences.ttsPitch().get()
    
    /**
     * Get saved voice ID
     */
    fun getVoice(): String = readerPreferences.ttsVoice().get()
    
    /**
     * Get auto-play next chapter setting
     */
    fun getAutoNextChapter(): Boolean = readerPreferences.ttsAutoPlay().get()
    
    /**
     * Get text highlight setting
     */
    fun getHighlightText(): Boolean = readerPreferences.ttsHighlightText().get()
    
    /**
     * Get skip empty lines setting
     */
    fun getSkipEmptyLines(): Boolean = readerPreferences.ttsSkipEmptyLines().get()
    
    /**
     * Get merge words count for remote TTS (chunk mode)
     */
    fun getMergeWordsRemote(): Int = readerPreferences.ttsMergeWordsRemote().get()
    
    /**
     * Get merge words count for native TTS
     */
    fun getMergeWordsNative(): Int = readerPreferences.ttsMergeWordsNative().get()
    
    /**
     * Get chapter cache enabled setting
     */
    fun isChapterCacheEnabled(): Boolean = readerPreferences.ttsChapterCacheEnabled().get()
    
    /**
     * Get chapter cache days setting
     */
    fun getChapterCacheDays(): Int = readerPreferences.ttsChapterCacheDays().get()
    
    // ========== Display Preferences ==========
    
    /**
     * Get custom colors enabled setting
     */
    fun useCustomColors(): Boolean = readerPreferences.ttsUseCustomColors().get()
    
    /**
     * Get background color
     */
    fun getBackgroundColor(): Long = readerPreferences.ttsBackgroundColor().get()
    
    /**
     * Get text color
     */
    fun getTextColor(): Long = readerPreferences.ttsTextColor().get()
    
    /**
     * Get font size
     */
    fun getFontSize(): Int = readerPreferences.ttsFontSize().get()
    
    /**
     * Get sentence highlight setting
     */
    fun getSentenceHighlight(): Boolean = readerPreferences.ttsSentenceHighlight().get()
    
    // ========== Preference Setters ==========
    
    /**
     * Set TTS speed
     */
    fun setSpeed(speed: Float) {
        readerPreferences.ttsSpeed().set(speed)
        controller?.dispatch(TTSCommand.SetSpeed(speed))
    }
    
    /**
     * Set TTS pitch
     */
    fun setPitch(pitch: Float) {
        readerPreferences.ttsPitch().set(pitch)
        controller?.dispatch(TTSCommand.SetPitch(pitch))
    }
    
    /**
     * Set voice ID
     */
    fun setVoice(voiceId: String) {
        readerPreferences.ttsVoice().set(voiceId)
    }
    
    /**
     * Set auto-play next chapter
     */
    fun setAutoNextChapter(enabled: Boolean) {
        readerPreferences.ttsAutoPlay().set(enabled)
        controller?.dispatch(TTSCommand.SetAutoNextChapter(enabled))
    }
    
    /**
     * Set merge words for remote TTS
     */
    fun setMergeWordsRemote(words: Int) {
        readerPreferences.ttsMergeWordsRemote().set(words)
    }
    
    /**
     * Set merge words for native TTS
     */
    fun setMergeWordsNative(words: Int) {
        readerPreferences.ttsMergeWordsNative().set(words)
    }
    
    /**
     * Set chapter cache enabled
     */
    fun setChapterCacheEnabled(enabled: Boolean) {
        readerPreferences.ttsChapterCacheEnabled().set(enabled)
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        Log.warn { "$TAG: cleanup()" }
        observerJob?.cancel()
        observerJob = null
        controller = null
        scope = null
    }
}
