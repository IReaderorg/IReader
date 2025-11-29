package ireader.domain.services.tts_service.player

import kotlinx.coroutines.flow.StateFlow

/**
 * Base interface for TTS Player State.
 * 
 * This provides a common contract for all TTS player implementations
 * (Native, Gradio, Piper, etc.) to expose their state.
 */
interface TTSPlayerState {
    /** Whether the player is currently playing audio */
    val isPlaying: StateFlow<Boolean>
    
    /** Whether playback is paused */
    val isPaused: StateFlow<Boolean>
    
    /** Whether the player is loading/generating audio */
    val isLoading: StateFlow<Boolean>
    
    /** Index of the current paragraph being played */
    val currentParagraph: StateFlow<Int>
    
    /** Total number of paragraphs in the content */
    val totalParagraphs: StateFlow<Int>
    
    /** Current speech speed (0.5 - 2.0) */
    val speed: StateFlow<Float>
    
    /** Current speech pitch (0.5 - 2.0) */
    val pitch: StateFlow<Float>
    
    /** Current error message, if any */
    val error: StateFlow<String?>
    
    /** Whether the player has been initialized with content */
    val hasContent: StateFlow<Boolean>
    
    /** Name of the current TTS engine being used */
    val engineName: StateFlow<String>
}

/**
 * Extended state interface for players that support caching (like Gradio TTS).
 */
interface CachingTTSPlayerState : TTSPlayerState {
    /** Set of paragraph indices that have been cached */
    val cachedParagraphs: StateFlow<Set<Int>>
    
    /** Set of paragraph indices currently being loaded */
    val loadingParagraphs: StateFlow<Set<Int>>
}
