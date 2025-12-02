package ireader.presentation.ui.reader.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ireader.domain.services.common.TTSPlaybackState
import ireader.domain.services.common.VoiceQuality
import ireader.i18n.UiText

/**
 * Sealed interface representing the TTS screen state.
 * 
 * This provides clear Idle/Playing/Paused/Error states for TTS playback.
 */
sealed interface TTSState {
    
    @Immutable
    data object Idle : TTSState
    
    @Immutable
    data object Initializing : TTSState
    
    @Immutable
    data class Ready(
        // Playback state
        val playbackState: TTSPlaybackState = TTSPlaybackState.IDLE,
        
        // Current playback info
        val currentSentenceIndex: Int = 0,
        val totalSentences: Int = 0,
        val playbackProgress: Float = 0f,
        val currentText: String = "",
        
        // Chapter info
        val chapterId: Long? = null,
        val chapterName: String = "",
        val bookId: Long? = null,
        val bookName: String = "",
        
        // Settings
        val speed: Float = 1.0f,
        val pitch: Float = 1.0f,
        val selectedVoiceId: String = "",
        val autoPlay: Boolean = false,
        val highlightText: Boolean = true,
        val skipEmptyLines: Boolean = true,
        
        // Available voices
        val availableVoices: List<TTSVoiceInfo> = emptyList(),
        val isLoadingVoices: Boolean = false,
        
        // Download state
        val downloadingVoiceId: String? = null,
        val downloadProgress: Float = 0f,
    ) : TTSState {
        
        /**
         * Check if TTS is currently playing
         */
        @Stable
        val isPlaying: Boolean
            get() = playbackState == TTSPlaybackState.PLAYING
        
        /**
         * Check if TTS is paused
         */
        @Stable
        val isPaused: Boolean
            get() = playbackState == TTSPlaybackState.PAUSED
        
        /**
         * Check if TTS is stopped/idle
         */
        @Stable
        val isStopped: Boolean
            get() = playbackState == TTSPlaybackState.IDLE
        
        /**
         * Check if TTS is initializing
         */
        @Stable
        val isInitializing: Boolean
            get() = playbackState == TTSPlaybackState.INITIALIZING
        
        /**
         * Check if there are available voices
         */
        @Stable
        val hasVoices: Boolean
            get() = availableVoices.isNotEmpty()
        
        /**
         * Get the currently selected voice
         */
        @Stable
        val selectedVoice: TTSVoiceInfo?
            get() = availableVoices.find { it.id == selectedVoiceId }
        
        /**
         * Check if can skip to next sentence
         */
        @Stable
        val canSkipNext: Boolean
            get() = currentSentenceIndex < totalSentences - 1
        
        /**
         * Check if can skip to previous sentence
         */
        @Stable
        val canSkipPrevious: Boolean
            get() = currentSentenceIndex > 0
        
        /**
         * Get formatted progress text (e.g., "5 / 20")
         */
        @Stable
        val progressText: String
            get() = "${currentSentenceIndex + 1} / $totalSentences"
        
        /**
         * Get formatted speed text (e.g., "1.0x")
         */
        @Stable
        val speedText: String
            get() = "${speed}x"
    }
    
    @Immutable
    data class Error(
        val message: UiText,
        val canRetry: Boolean = true,
    ) : TTSState
}

// TTSVoiceInfo is defined in ReaderTTSViewModel.kt to avoid duplication

/**
 * Sealed interface for TTS dialogs
 */
sealed interface TTSDialog {
    data object None : TTSDialog
    data object VoiceSelector : TTSDialog
    data object SpeedSelector : TTSDialog
    data object PitchSelector : TTSDialog
    data object Settings : TTSDialog
    data class DownloadVoice(val voiceId: String, val voiceName: String) : TTSDialog
}

/**
 * Sealed class for TTS events (one-time events)
 */
sealed class TTSEvent {
    data class ShowSnackbar(val message: UiText) : TTSEvent()
    data object PlaybackStarted : TTSEvent()
    data object PlaybackPaused : TTSEvent()
    data object PlaybackStopped : TTSEvent()
    data object PlaybackCompleted : TTSEvent()
    data class ChapterCompleted(val chapterId: Long) : TTSEvent()
    data class VoiceDownloaded(val voiceId: String) : TTSEvent()
    data class Error(val message: UiText) : TTSEvent()
}
