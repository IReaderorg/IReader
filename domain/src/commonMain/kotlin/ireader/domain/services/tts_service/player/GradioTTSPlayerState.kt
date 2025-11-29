package ireader.domain.services.tts_service.player

import kotlinx.coroutines.flow.StateFlow

/**
 * Represents the current state of the Gradio TTS Player.
 * 
 * This is an immutable data class that captures all aspects of the player's state
 * at any given moment. State changes are communicated through StateFlow.
 */
data class GradioTTSPlayerState(
    /** Whether the player is currently playing audio */
    val isPlaying: Boolean = false,
    
    /** Whether playback is paused */
    val isPaused: Boolean = false,
    
    /** Whether the player is loading/generating audio */
    val isLoading: Boolean = false,
    
    /** Index of the current paragraph being played */
    val currentParagraph: Int = 0,
    
    /** Total number of paragraphs in the content */
    val totalParagraphs: Int = 0,
    
    /** Set of paragraph indices that have been cached */
    val cachedParagraphs: Set<Int> = emptySet(),
    
    /** Set of paragraph indices currently being loaded */
    val loadingParagraphs: Set<Int> = emptySet(),
    
    /** Current speech speed (0.5 - 2.0) */
    val speed: Float = 1.0f,
    
    /** Current speech pitch (0.5 - 2.0) */
    val pitch: Float = 1.0f,
    
    /** Current error message, if any */
    val error: String? = null,
    
    /** Whether the player has been initialized with content */
    val hasContent: Boolean = false,
    
    /** Progress within current paragraph (0.0 - 1.0) */
    val paragraphProgress: Float = 0f,
    
    /** Name of the current TTS engine being used */
    val engineName: String = ""
) {
    /** Whether the player can play (has content and is not at the end) */
    val canPlay: Boolean get() = hasContent && currentParagraph < totalParagraphs
    
    /** Whether the player can go to next paragraph */
    val canNext: Boolean get() = hasContent && currentParagraph < totalParagraphs - 1
    
    /** Whether the player can go to previous paragraph */
    val canPrevious: Boolean get() = hasContent && currentParagraph > 0
    
    /** Whether the player is actively doing something (playing or loading) */
    val isActive: Boolean get() = isPlaying || isLoading
    
    /** Percentage of paragraphs that have been cached */
    val cacheProgress: Float get() = if (totalParagraphs > 0) {
        cachedParagraphs.size.toFloat() / totalParagraphs
    } else 0f
}

/**
 * Events emitted by the Gradio TTS Player.
 * 
 * These events can be observed to react to player state changes,
 * update UI, or trigger other actions.
 */
sealed class GradioTTSPlayerEvent {
    /** Playback has started */
    object PlaybackStarted : GradioTTSPlayerEvent()
    
    /** Playback has been paused */
    object PlaybackPaused : GradioTTSPlayerEvent()
    
    /** Playback has been stopped */
    object PlaybackStopped : GradioTTSPlayerEvent()
    
    /** Playback has been resumed */
    object PlaybackResumed : GradioTTSPlayerEvent()
    
    /** Current paragraph has changed */
    data class ParagraphChanged(val index: Int, val text: String) : GradioTTSPlayerEvent()
    
    /** A paragraph has been cached */
    data class ParagraphCached(val index: Int) : GradioTTSPlayerEvent()
    
    /** All paragraphs in the chapter have been read */
    object ChapterFinished : GradioTTSPlayerEvent()
    
    /** An error occurred */
    data class Error(val message: String, val recoverable: Boolean = true) : GradioTTSPlayerEvent()
    
    /** Speed setting has changed */
    data class SpeedChanged(val speed: Float) : GradioTTSPlayerEvent()
    
    /** Pitch setting has changed */
    data class PitchChanged(val pitch: Float) : GradioTTSPlayerEvent()
    
    /** Content has been loaded */
    data class ContentLoaded(val paragraphCount: Int) : GradioTTSPlayerEvent()
    
    /** Cache has been cleared */
    object CacheCleared : GradioTTSPlayerEvent()
    
    /** Player is being released */
    object Releasing : GradioTTSPlayerEvent()
}

/**
 * Commands that can be sent to the Gradio TTS Player.
 * 
 * These commands are processed sequentially to ensure thread-safe
 * state management.
 */
sealed class GradioTTSPlayerCommand {
    /** Set content to be read */
    data class SetContent(
        val paragraphs: List<String>,
        val startIndex: Int = 0
    ) : GradioTTSPlayerCommand()
    
    /** Start or resume playback */
    object Play : GradioTTSPlayerCommand()
    
    /** Pause playback */
    object Pause : GradioTTSPlayerCommand()
    
    /** Stop playback and reset to beginning */
    object Stop : GradioTTSPlayerCommand()
    
    /** Skip to next paragraph */
    object Next : GradioTTSPlayerCommand()
    
    /** Skip to previous paragraph */
    object Previous : GradioTTSPlayerCommand()
    
    /** Jump to specific paragraph */
    data class JumpTo(val index: Int) : GradioTTSPlayerCommand()
    
    /** Set speech speed */
    data class SetSpeed(val speed: Float) : GradioTTSPlayerCommand()
    
    /** Set speech pitch */
    data class SetPitch(val pitch: Float) : GradioTTSPlayerCommand()
    
    /** Clear audio cache */
    object ClearCache : GradioTTSPlayerCommand()
    
    /** Release all resources */
    object Release : GradioTTSPlayerCommand()
}
