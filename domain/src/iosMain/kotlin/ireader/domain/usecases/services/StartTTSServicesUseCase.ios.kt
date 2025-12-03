package ireader.domain.usecases.services

import platform.AVFAudio.*
import platform.Foundation.*
import platform.MediaPlayer.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*

/**
 * iOS implementation of StartTTSServicesUseCase
 */
@OptIn(ExperimentalForeignApi::class)
actual class StartTTSServicesUseCase {
    
    private var synthesizer: AVSpeechSynthesizer? = null
    private var currentBookId: Long? = null
    private var currentChapterId: Long? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    companion object {
        const val COMMAND_PLAY = 1
        const val COMMAND_PAUSE = 2
        const val COMMAND_STOP = 3
        const val COMMAND_NEXT = 4
        const val COMMAND_PREVIOUS = 5
        const val COMMAND_TOGGLE = 6
    }
    
    init {
        setupAudioSession()
        setupRemoteCommandCenter()
    }
    
    actual operator fun invoke(command: Int, bookId: Long?, chapterId: Long?) {
        when (command) {
            COMMAND_PLAY -> {
                if (bookId != null) {
                    currentBookId = bookId
                    currentChapterId = chapterId
                    startReading(bookId, chapterId)
                } else {
                    resume()
                }
            }
            COMMAND_PAUSE -> pause()
            COMMAND_STOP -> stop()
            COMMAND_NEXT -> nextChapter()
            COMMAND_PREVIOUS -> previousChapter()
            COMMAND_TOGGLE -> togglePlayPause()
        }
    }
    
    private fun setupAudioSession() {
        val audioSession = AVAudioSession.sharedInstance()
        try {
            audioSession.setCategory(AVAudioSessionCategoryPlayback, AVAudioSessionModeSpokenAudio, AVAudioSessionCategoryOptionDuckOthers, null)
            audioSession.setActive(true, null)
        } catch (e: Exception) {
            println("[TTS] Failed to set up audio session: ${e.message}")
        }
    }

    private fun setupRemoteCommandCenter() {
        val commandCenter = MPRemoteCommandCenter.sharedCommandCenter()
        
        commandCenter.playCommand.enabled = true
        commandCenter.playCommand.addTargetWithHandler { _ -> resume(); MPRemoteCommandHandlerStatusSuccess }
        
        commandCenter.pauseCommand.enabled = true
        commandCenter.pauseCommand.addTargetWithHandler { _ -> pause(); MPRemoteCommandHandlerStatusSuccess }
        
        commandCenter.togglePlayPauseCommand.enabled = true
        commandCenter.togglePlayPauseCommand.addTargetWithHandler { _ -> togglePlayPause(); MPRemoteCommandHandlerStatusSuccess }
        
        commandCenter.nextTrackCommand.enabled = true
        commandCenter.nextTrackCommand.addTargetWithHandler { _ -> nextChapter(); MPRemoteCommandHandlerStatusSuccess }
        
        commandCenter.previousTrackCommand.enabled = true
        commandCenter.previousTrackCommand.addTargetWithHandler { _ -> previousChapter(); MPRemoteCommandHandlerStatusSuccess }
        
        commandCenter.stopCommand.enabled = true
        commandCenter.stopCommand.addTargetWithHandler { _ -> stop(); MPRemoteCommandHandlerStatusSuccess }
    }
    
    private fun startReading(bookId: Long, chapterId: Long?) {
        if (synthesizer == null) synthesizer = AVSpeechSynthesizer()
        scope.launch {
            try {
                println("[TTS] Starting to read book $bookId, chapter $chapterId")
            } catch (e: Exception) {
                println("[TTS] Error starting reading: ${e.message}")
            }
        }
    }
    
    private fun speakText(text: String) {
        val synth = synthesizer ?: return
        if (synth.isSpeaking()) synth.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        
        val utterance = AVSpeechUtterance.speechUtteranceWithString(text).apply {
            rate = AVSpeechUtteranceDefaultSpeechRate
            pitchMultiplier = 1.0f
            volume = 1.0f
            voice = AVSpeechSynthesisVoice.voiceWithLanguage(NSLocale.currentLocale.languageCode)
        }
        
        synth.speakUtterance(utterance)
        updateNowPlayingInfo(isPlaying = true)
    }
    
    private fun pause() {
        synthesizer?.pauseSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        updateNowPlayingInfo(isPlaying = false)
    }
    
    private fun resume() {
        synthesizer?.continueSpeaking()
        updateNowPlayingInfo(isPlaying = true)
    }
    
    private fun stop() {
        synthesizer?.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = null
        try { AVAudioSession.sharedInstance().setActive(false, null) } catch (e: Exception) {}
        currentBookId = null
        currentChapterId = null
    }
    
    private fun togglePlayPause() {
        val synth = synthesizer ?: return
        if (synth.isPaused()) resume()
        else if (synth.isSpeaking()) pause()
        else currentBookId?.let { startReading(it, currentChapterId) }
    }
    
    private fun nextChapter() { scope.launch { println("[TTS] Next chapter requested") } }
    private fun previousChapter() { scope.launch { println("[TTS] Previous chapter requested") } }
    
    private fun updateNowPlayingInfo(isPlaying: Boolean) {
        val info = mutableMapOf<Any?, Any?>()
        info[MPMediaItemPropertyTitle] = "IReader TTS"
        info[MPMediaItemPropertyArtist] = "Reading..."
        info[MPNowPlayingInfoPropertyPlaybackRate] = if (isPlaying) 1.0 else 0.0
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = info
    }
}
