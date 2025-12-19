package ireader.domain.services.tts_service.v2

/**
 * TTS Commands - Sealed class for all possible TTS operations
 * 
 * All interactions with TTS go through commands.
 * This makes the system predictable and easy to debug.
 */
sealed class TTSCommand {
    // Playback commands
    object Play : TTSCommand()
    object Pause : TTSCommand()
    object Stop : TTSCommand()
    object Resume : TTSCommand()
    
    // Navigation commands
    object NextParagraph : TTSCommand()
    object PreviousParagraph : TTSCommand()
    data class JumpToParagraph(val index: Int) : TTSCommand()
    object NextChapter : TTSCommand()
    object PreviousChapter : TTSCommand()
    
    // Content commands
    data class LoadChapter(
        val bookId: Long,
        val chapterId: Long,
        val startParagraph: Int = 0
    ) : TTSCommand()
    data class SetContent(val paragraphs: List<String>) : TTSCommand()
    /** Refresh current chapter content (e.g., when content filter settings change) */
    object RefreshContent : TTSCommand()
    
    // Translation commands
    data class SetTranslatedContent(val paragraphs: List<String>?) : TTSCommand()
    data class ToggleTranslation(val show: Boolean) : TTSCommand()
    data class ToggleBilingualMode(val enabled: Boolean) : TTSCommand()
    
    // Sentence highlighting commands
    data class SetSentenceHighlight(val enabled: Boolean) : TTSCommand()
    data class UpdateParagraphStartTime(val timeMs: Long) : TTSCommand()
    data class SetCalibration(val wpm: Float?, val isCalibrated: Boolean) : TTSCommand()
    
    // Settings commands
    data class SetSpeed(val speed: Float) : TTSCommand()
    data class SetPitch(val pitch: Float) : TTSCommand()
    data class SetAutoNextChapter(val enabled: Boolean) : TTSCommand()
    data class SetEngine(val type: EngineType) : TTSCommand()
    data class SetGradioConfig(val config: GradioConfig) : TTSCommand()
    
    // Chunk mode commands (for remote TTS with text merging)
    data class EnableChunkMode(val targetWordCount: Int = 50) : TTSCommand()
    object DisableChunkMode : TTSCommand()
    object NextChunk : TTSCommand()
    object PreviousChunk : TTSCommand()
    data class JumpToChunk(val index: Int) : TTSCommand()
    
    // Lifecycle commands
    object Initialize : TTSCommand()
    /** Stop playback and release engine, but keep content (book, chapter, paragraphs) */
    object StopAndRelease : TTSCommand()
    /** Full cleanup - resets everything including content */
    object Cleanup : TTSCommand()
}

/**
 * TTS Events - Emitted by the controller for one-time events
 * 
 * Unlike state (which is continuous), events are discrete occurrences.
 */
sealed class TTSEvent {
    object PlaybackStarted : TTSEvent()
    object PlaybackPaused : TTSEvent()
    object PlaybackStopped : TTSEvent()
    object ParagraphCompleted : TTSEvent()
    object ChapterCompleted : TTSEvent()
    data class Error(val error: TTSError) : TTSEvent()
}
