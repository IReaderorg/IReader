package ireader.domain.services.tts_service.v2

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter

/**
 * TTS State - Immutable data class representing the complete TTS state
 * 
 * This is the SINGLE SOURCE OF TRUTH for all TTS-related state.
 * UI observes this via StateFlow, never mutates directly.
 */
data class TTSState(
    // Playback state
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val currentParagraphIndex: Int = 0,
    val previousParagraphIndex: Int = 0,
    val totalParagraphs: Int = 0,
    
    // Content
    val book: Book? = null,
    val chapter: Chapter? = null,
    val paragraphs: List<String> = emptyList(),
    
    // Translation support
    val translatedParagraphs: List<String>? = null,
    val showTranslation: Boolean = false,
    val bilingualMode: Boolean = false,
    val isTranslationAvailable: Boolean = false,
    
    // Settings
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val autoNextChapter: Boolean = false,
    
    // Engine info
    val engineType: EngineType = EngineType.NATIVE,
    val isEngineReady: Boolean = false,
    
    // Sentence highlighting
    val paragraphStartTime: Long = 0L,
    val sentenceHighlightEnabled: Boolean = false,
    val calibratedWPM: Float? = null,
    val isCalibrated: Boolean = false,
    
    // Chunk mode (for remote TTS with text merging)
    val chunkModeEnabled: Boolean = false,
    val currentChunkIndex: Int = 0,
    val totalChunks: Int = 0,
    val currentChunkParagraphs: List<Int> = emptyList(),
    val cachedChunks: Set<Int> = emptySet(),
    val isUsingCachedAudio: Boolean = false,
    
    // Cache/download state
    val cachedParagraphs: Set<Int> = emptySet(),
    val loadingParagraphs: Set<Int> = emptySet(),
    
    // Sleep timer (managed externally but reflected here for UI)
    val sleepTimeRemaining: Long = 0L,
    val sleepModeEnabled: Boolean = false,
    
    // Error state
    val error: TTSError? = null
) {
    val isPlaying: Boolean get() = playbackState == PlaybackState.PLAYING
    val isPaused: Boolean get() = playbackState == PlaybackState.PAUSED
    val isLoading: Boolean get() = playbackState == PlaybackState.LOADING
    val hasContent: Boolean get() = paragraphs.isNotEmpty()
    val canGoNext: Boolean get() = currentParagraphIndex < paragraphs.lastIndex
    val canGoPrevious: Boolean get() = currentParagraphIndex > 0
    val progress: Float get() = if (totalParagraphs > 0) 
        (currentParagraphIndex + 1).toFloat() / totalParagraphs else 0f
    
    // Translation helpers
    val hasTranslation: Boolean get() = translatedParagraphs != null && translatedParagraphs.isNotEmpty()
    val displayContent: List<String> get() = if (showTranslation && hasTranslation) 
        translatedParagraphs!! else paragraphs
    
    // Chunk mode helpers
    val canGoNextChunk: Boolean get() = chunkModeEnabled && currentChunkIndex < totalChunks - 1
    val canGoPreviousChunk: Boolean get() = chunkModeEnabled && currentChunkIndex > 0
    val chunkProgress: Float get() = if (totalChunks > 0) 
        (currentChunkIndex + 1).toFloat() / totalChunks else 0f
    val isCurrentChunkCached: Boolean get() = currentChunkIndex in cachedChunks
    val allChunksCached: Boolean get() = chunkModeEnabled && cachedChunks.size >= totalChunks
    
    // Check if a paragraph is in current merged chunk
    fun isParagraphInCurrentChunk(index: Int): Boolean = 
        chunkModeEnabled && index in currentChunkParagraphs
}

enum class PlaybackState {
    IDLE,       // Not started
    LOADING,    // Loading content or initializing engine
    PLAYING,    // Actively speaking
    PAUSED,     // Paused by user
    STOPPED,    // Stopped (can resume from beginning)
    ERROR       // Error occurred
}

enum class EngineType {
    NATIVE,     // Platform native TTS (Android TTS, AVSpeechSynthesizer, etc.)
    GRADIO      // Remote Gradio-based TTS (Coqui, etc.)
}

/**
 * TTS Errors - Sealed class for type-safe error handling
 */
sealed class TTSError {
    data class EngineInitFailed(val message: String) : TTSError()
    data class ContentLoadFailed(val message: String) : TTSError()
    data class SpeechFailed(val message: String) : TTSError()
    data class NetworkError(val message: String) : TTSError()
    object NoContent : TTSError()
    object EngineNotReady : TTSError()
}
