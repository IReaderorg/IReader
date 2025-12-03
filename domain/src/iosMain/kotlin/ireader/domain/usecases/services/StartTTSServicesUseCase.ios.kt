package ireader.domain.usecases.services

import platform.AVFAudio.*
import platform.Foundation.*
import platform.MediaPlayer.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.core.source.model.Text

/**
 * iOS implementation of StartTTSServicesUseCase
 * 
 * Uses Koin Service Locator pattern to inject dependencies since expect/actual
 * classes don't support constructor parameters in commonMain.
 * 
 * Features:
 * - AVSpeechSynthesizer for native iOS TTS
 * - MPRemoteCommandCenter for lock screen/Control Center controls
 * - MPNowPlayingInfoCenter for Now Playing info
 * - Full chapter reading with paragraph navigation
 */
@OptIn(ExperimentalForeignApi::class)
actual class StartTTSServicesUseCase : KoinComponent {
    
    // Dependencies injected via Koin Service Locator
    private val bookRepo: BookRepository by inject()
    private val chapterRepo: ChapterRepository by inject()
    
    private var synthesizer: AVSpeechSynthesizer? = null
    private var currentBookId: Long? = null
    private var currentChapterId: Long? = null
    private var currentBook: Book? = null
    private var currentChapter: Chapter? = null
    private var chapters: List<Chapter> = emptyList()
    private var currentParagraphIndex: Int = 0
    private var paragraphs: List<String> = emptyList()
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var readingJob: Job? = null
    
    // TTS settings
    private var speechRate: Float = AVSpeechUtteranceDefaultSpeechRate
    private var speechPitch: Float = 1.0f
    private var speechVolume: Float = 1.0f
    private var autoNextChapter: Boolean = true
    
    companion object {
        const val COMMAND_PLAY = 1
        const val COMMAND_PAUSE = 2
        const val COMMAND_STOP = 3
        const val COMMAND_NEXT = 4
        const val COMMAND_PREVIOUS = 5
        const val COMMAND_TOGGLE = 6
        const val COMMAND_NEXT_PAR = 7
        const val COMMAND_PREV_PAR = 8
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
            COMMAND_NEXT_PAR -> nextParagraph()
            COMMAND_PREV_PAR -> previousParagraph()
        }
    }
    
    private fun setupAudioSession() {
        val audioSession = AVAudioSession.sharedInstance()
        try {
            audioSession.setCategory(AVAudioSessionCategoryPlayback, AVAudioSessionModeSpokenAudio, AVAudioSessionCategoryOptionDuckOthers, null)
            audioSession.setActive(true, null)
            println("[TTS] Audio session configured successfully")
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
        
        println("[TTS] Remote command center configured")
    }
    
    private fun startReading(bookId: Long, chapterId: Long?) {
        readingJob?.cancel()
        
        readingJob = scope.launch {
            try {
                println("[TTS] Starting to read book $bookId, chapter $chapterId")
                
                // Load book and chapters
                currentBook = bookRepo.findBookById(bookId)
                chapters = chapterRepo.findChaptersByBookId(bookId)
                
                if (currentBook == null) {
                    println("[TTS] Book not found: $bookId")
                    return@launch
                }
                
                // Find the chapter to read
                val chapter = if (chapterId != null) {
                    chapterRepo.findChapterById(chapterId)
                } else {
                    // Find first unread chapter or first chapter
                    chapters.firstOrNull { !it.read } ?: chapters.firstOrNull()
                }
                
                if (chapter == null) {
                    println("[TTS] No chapter found to read")
                    return@launch
                }
                
                currentChapter = chapter
                currentChapterId = chapter.id
                
                // Extract text content from chapter
                paragraphs = extractTextContent(chapter)
                
                if (paragraphs.isEmpty()) {
                    println("[TTS] Chapter has no text content")
                    return@launch
                }
                
                currentParagraphIndex = 0
                
                // Initialize synthesizer
                if (synthesizer == null) {
                    synthesizer = AVSpeechSynthesizer()
                }
                
                // Update Now Playing info
                updateNowPlayingInfo(isPlaying = true)
                
                // Start reading from current paragraph
                readCurrentParagraph()
                
            } catch (e: Exception) {
                println("[TTS] Error starting reading: ${e.message}")
            }
        }
    }
    
    private fun extractTextContent(chapter: Chapter): List<String> {
        return chapter.content
            .filterIsInstance<Text>()
            .map { it.text }
            .filter { it.isNotBlank() }
            .map { it.trim() }
    }
    
    private fun readCurrentParagraph() {
        if (currentParagraphIndex >= paragraphs.size) {
            // Finished current chapter
            if (autoNextChapter) {
                nextChapter()
            } else {
                stop()
            }
            return
        }
        
        val text = paragraphs[currentParagraphIndex]
        speakText(text) {
            // On completion, move to next paragraph
            currentParagraphIndex++
            readCurrentParagraph()
        }
    }
    
    private fun speakText(text: String, onComplete: (() -> Unit)? = null) {
        val synth = synthesizer ?: return
        
        // Stop any current speech
        if (synth.isSpeaking()) {
            synth.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        }
        
        val utterance = AVSpeechUtterance.speechUtteranceWithString(text).apply {
            rate = speechRate
            pitchMultiplier = speechPitch
            volume = speechVolume
            voice = AVSpeechSynthesisVoice.voiceWithLanguage(NSLocale.currentLocale.languageCode)
        }
        
        // Note: For completion callback, we'd need to implement AVSpeechSynthesizerDelegate
        // For now, we estimate completion time based on text length
        synth.speakUtterance(utterance)
        
        // Estimate speech duration and schedule next paragraph
        if (onComplete != null) {
            val estimatedDuration = (text.length * 60.0 / (speechRate * 150)).toLong() * 1000 // rough estimate
            scope.launch {
                delay(estimatedDuration.coerceAtLeast(500))
                // Check if still speaking (user might have paused/stopped)
                if (synth.isSpeaking() || synth.isPaused()) {
                    // Wait for actual completion
                    while (synth.isSpeaking()) {
                        delay(100)
                    }
                }
                if (!synth.isPaused()) {
                    onComplete()
                }
            }
        }
        
        updateNowPlayingInfo(isPlaying = true)
    }
    
    private fun pause() {
        synthesizer?.pauseSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        updateNowPlayingInfo(isPlaying = false)
        println("[TTS] Paused")
    }
    
    private fun resume() {
        val synth = synthesizer
        if (synth != null && synth.isPaused()) {
            synth.continueSpeaking()
            updateNowPlayingInfo(isPlaying = true)
            println("[TTS] Resumed")
        } else if (currentBookId != null) {
            // Restart reading from current position
            readCurrentParagraph()
        }
    }
    
    private fun stop() {
        readingJob?.cancel()
        readingJob = null
        synthesizer?.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = null
        try { AVAudioSession.sharedInstance().setActive(false, null) } catch (e: Exception) {}
        currentBookId = null
        currentChapterId = null
        currentBook = null
        currentChapter = null
        paragraphs = emptyList()
        currentParagraphIndex = 0
        println("[TTS] Stopped")
    }
    
    private fun togglePlayPause() {
        val synth = synthesizer ?: return
        if (synth.isPaused()) {
            resume()
        } else if (synth.isSpeaking()) {
            pause()
        } else {
            currentBookId?.let { startReading(it, currentChapterId) }
        }
    }
    
    private fun nextChapter() {
        scope.launch {
            val currentIndex = chapters.indexOfFirst { it.id == currentChapterId }
            if (currentIndex >= 0 && currentIndex < chapters.size - 1) {
                val nextChapter = chapters[currentIndex + 1]
                currentChapterId = nextChapter.id
                currentChapter = nextChapter
                paragraphs = extractTextContent(nextChapter)
                currentParagraphIndex = 0
                
                println("[TTS] Moving to next chapter: ${nextChapter.name}")
                updateNowPlayingInfo(isPlaying = true)
                readCurrentParagraph()
            } else {
                println("[TTS] No more chapters")
                stop()
            }
        }
    }
    
    private fun previousChapter() {
        scope.launch {
            val currentIndex = chapters.indexOfFirst { it.id == currentChapterId }
            if (currentIndex > 0) {
                val prevChapter = chapters[currentIndex - 1]
                currentChapterId = prevChapter.id
                currentChapter = prevChapter
                paragraphs = extractTextContent(prevChapter)
                currentParagraphIndex = 0
                
                println("[TTS] Moving to previous chapter: ${prevChapter.name}")
                updateNowPlayingInfo(isPlaying = true)
                readCurrentParagraph()
            } else {
                println("[TTS] Already at first chapter")
            }
        }
    }
    
    private fun nextParagraph() {
        if (currentParagraphIndex < paragraphs.size - 1) {
            synthesizer?.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
            currentParagraphIndex++
            readCurrentParagraph()
            println("[TTS] Next paragraph: ${currentParagraphIndex + 1}/${paragraphs.size}")
        }
    }
    
    private fun previousParagraph() {
        if (currentParagraphIndex > 0) {
            synthesizer?.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
            currentParagraphIndex--
            readCurrentParagraph()
            println("[TTS] Previous paragraph: ${currentParagraphIndex + 1}/${paragraphs.size}")
        }
    }
    
    private fun updateNowPlayingInfo(isPlaying: Boolean) {
        val info = mutableMapOf<Any?, Any?>()
        info[MPMediaItemPropertyTitle] = currentChapter?.name ?: "IReader TTS"
        info[MPMediaItemPropertyArtist] = currentBook?.title ?: "Reading..."
        info[MPNowPlayingInfoPropertyPlaybackRate] = if (isPlaying) 1.0 else 0.0
        
        // Add progress info if available
        if (paragraphs.isNotEmpty()) {
            info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = currentParagraphIndex.toDouble()
            info[MPMediaItemPropertyPlaybackDuration] = paragraphs.size.toDouble()
        }
        
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = info
    }
    
    // Settings methods for external configuration
    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(AVSpeechUtteranceMinimumSpeechRate, AVSpeechUtteranceMaximumSpeechRate)
    }
    
    fun setSpeechPitch(pitch: Float) {
        speechPitch = pitch.coerceIn(0.5f, 2.0f)
    }
    
    fun setSpeechVolume(volume: Float) {
        speechVolume = volume.coerceIn(0.0f, 1.0f)
    }
    
    fun setAutoNextChapter(enabled: Boolean) {
        autoNextChapter = enabled
    }
}
