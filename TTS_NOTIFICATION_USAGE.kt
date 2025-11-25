/**
 * TTS Notification Manager Usage Examples
 * 
 * Shows how to use the TTS notification abstraction for both
 * Native TTS and Coqui TTS on Android and Desktop platforms.
 */

package ireader.examples

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.tts.*

/**
 * Example 1: Basic Setup (Android)
 */
fun example1_AndroidSetup(context: Context, mediaSession: MediaSessionCompat) {
    // Create notification manager
    val notificationManager = AndroidTTSNotificationManager(context, mediaSession)
    
    // Create use case
    val manageTTSNotification = ManageTTSNotification(notificationManager)
    
    // Set up callbacks
    manageTTSNotification.setCallback(
        onPlayPause = { println("Play/Pause clicked") },
        onStop = { println("Stop clicked") },
        onNext = { println("Next clicked") },
        onPrevious = { println("Previous clicked") }
    )
    
    println("✅ Android TTS notification manager set up")
}

/**
 * Example 2: Basic Setup (Desktop)
 */
fun example2_DesktopSetup() {
    // Create notification manager
    val notificationManager = DesktopTTSNotificationManager()
    
    // Create use case
    val manageTTSNotification = ManageTTSNotification(notificationManager)
    
    println("✅ Desktop TTS notification manager set up")
}

/**
 * Example 3: Show Notification for Native TTS
 */
fun example3_ShowNativeTTS(
    manageTTSNotification: ManageTTSNotification,
    book: Book,
    chapter: Chapter
) {
    val state = ttsNotificationState {
        playing(true)
        paragraph(0, 10)
        bookTitle(book.title)
        chapterTitle(chapter.name)
        speed(1.0f)
        provider("Native TTS")
    }
    
    manageTTSNotification.show(book, chapter, state)
    println("✅ Native TTS notification shown")
}

/**
 * Example 4: Show Notification for Coqui TTS
 */
fun example4_ShowCoquiTTS(
    manageTTSNotification: ManageTTSNotification,
    book: Book,
    chapter: Chapter
) {
    val state = ttsNotificationState {
        playing(true)
        paragraph(0, 15)
        bookTitle(book.title)
        chapterTitle(chapter.name)
        coverUrl(book.cover)
        speed(1.2f)
        provider("Coqui TTS")
    }
    
    manageTTSNotification.show(book, chapter, state)
    println("✅ Coqui TTS notification shown")
}

/**
 * Example 5: Update Progress
 */
fun example5_UpdateProgress(manageTTSNotification: ManageTTSNotification) {
    // Update to paragraph 5 of 10
    manageTTSNotification.updateProgress(
        currentParagraph = 5,
        totalParagraphs = 10
    )
    
    println("✅ Progress updated: 5/10")
}

/**
 * Example 6: Update Playback State
 */
fun example6_UpdatePlaybackState(manageTTSNotification: ManageTTSNotification) {
    // Pause
    manageTTSNotification.updatePlaybackState(
        isPlaying = false,
        isPaused = true
    )
    println("⏸️ Paused")
    
    // Resume
    manageTTSNotification.updatePlaybackState(
        isPlaying = true,
        isPaused = false
    )
    println("▶️ Playing")
}

/**
 * Example 7: Update Speed
 */
fun example7_UpdateSpeed(manageTTSNotification: ManageTTSNotification) {
    manageTTSNotification.updateSpeed(1.5f)
    println("⚡ Speed: 1.5x")
}

/**
 * Example 8: Complete Reading Flow
 */
fun example8_CompleteFlow(
    manageTTSNotification: ManageTTSNotification,
    book: Book,
    chapters: List<Chapter>
) {
    var currentChapter = 0
    var currentParagraph = 0
    val paragraphsPerChapter = 10
    
    // Start reading
    val initialState = ttsNotificationState {
        playing(true)
        paragraph(0, paragraphsPerChapter)
        bookTitle(book.title)
        chapterTitle(chapters[0].name)
        speed(1.0f)
        provider("Coqui TTS")
    }
    
    manageTTSNotification.show(book, chapters[0], initialState)
    
    // Simulate reading progress
    for (i in 0 until paragraphsPerChapter) {
        currentParagraph = i
        manageTTSNotification.updateProgress(currentParagraph, paragraphsPerChapter)
        println("📄 Reading paragraph $i/$paragraphsPerChapter")
        Thread.sleep(1000) // Simulate reading time
    }
    
    // Move to next chapter
    currentChapter++
    if (currentChapter < chapters.size) {
        val nextState = ttsNotificationState {
            playing(true)
            paragraph(0, paragraphsPerChapter)
            bookTitle(book.title)
            chapterTitle(chapters[currentChapter].name)
            speed(1.0f)
            provider("Coqui TTS")
        }
        
        manageTTSNotification.show(book, chapters[currentChapter], nextState)
        println("⏭️ Next chapter: ${chapters[currentChapter].name}")
    }
    
    // Stop reading
    manageTTSNotification.hide()
    println("⏹️ Stopped")
}

/**
 * Example 9: Switch Between TTS Providers
 */
fun example9_SwitchProviders(
    manageTTSNotification: ManageTTSNotification,
    book: Book,
    chapter: Chapter,
    useNativeTTS: Boolean
) {
    val provider = if (useNativeTTS) "Native TTS" else "Coqui TTS"
    
    val state = ttsNotificationState {
        playing(true)
        paragraph(5, 10)
        bookTitle(book.title)
        chapterTitle(chapter.name)
        speed(1.0f)
        provider(provider)
    }
    
    manageTTSNotification.update(state)
    println("🔄 Switched to $provider")
}

/**
 * Example 10: Handle Notification Actions
 */
fun example10_HandleActions(
    manageTTSNotification: ManageTTSNotification,
    onPlayPauseAction: () -> Unit,
    onStopAction: () -> Unit,
    onNextAction: () -> Unit,
    onPreviousAction: () -> Unit
) {
    manageTTSNotification.setCallback(
        onPlayPause = {
            println("🎵 Play/Pause action")
            onPlayPauseAction()
        },
        onStop = {
            println("⏹️ Stop action")
            onStopAction()
            manageTTSNotification.hide()
        },
        onNext = {
            println("⏭️ Next action")
            onNextAction()
        },
        onPrevious = {
            println("⏮️ Previous action")
            onPreviousAction()
        },
        onSeek = { position ->
            println("⏩ Seek to position: $position")
        },
        onSpeedChange = { speed ->
            println("⚡ Speed changed to: ${speed}x")
            manageTTSNotification.updateSpeed(speed)
        }
    )
}

/**
 * Example 11: Integration with Native TTS Service
 */
class NativeTTSIntegration(
    private val manageTTSNotification: ManageTTSNotification
) {
    private var isPlaying = false
    private var currentParagraph = 0
    private var totalParagraphs = 0
    
    fun startReading(book: Book, chapter: Chapter, paragraphs: List<String>) {
        totalParagraphs = paragraphs.size
        currentParagraph = 0
        isPlaying = true
        
        val state = ttsNotificationState {
            playing(true)
            paragraph(0, totalParagraphs)
            bookTitle(book.title)
            chapterTitle(chapter.name)
            speed(1.0f)
            provider("Native TTS")
        }
        
        manageTTSNotification.show(book, chapter, state)
    }
    
    fun onParagraphComplete(index: Int) {
        currentParagraph = index + 1
        manageTTSNotification.updateProgress(currentParagraph, totalParagraphs)
    }
    
    fun pause() {
        isPlaying = false
        manageTTSNotification.updatePlaybackState(isPlaying = false, isPaused = true)
    }
    
    fun resume() {
        isPlaying = true
        manageTTSNotification.updatePlaybackState(isPlaying = true, isPaused = false)
    }
    
    fun stop() {
        isPlaying = false
        manageTTSNotification.hide()
    }
}

/**
 * Example 12: Integration with Coqui TTS Service
 */
class CoquiTTSIntegration(
    private val manageTTSNotification: ManageTTSNotification
) {
    private var isPlaying = false
    private var currentParagraph = 0
    private var totalParagraphs = 0
    
    fun startReading(book: Book, chapter: Chapter, paragraphs: List<String>, speed: Float) {
        totalParagraphs = paragraphs.size
        currentParagraph = 0
        isPlaying = true
        
        val state = ttsNotificationState {
            playing(true)
            paragraph(0, totalParagraphs)
            bookTitle(book.title)
            chapterTitle(chapter.name)
            coverUrl(book.cover)
            speed(speed)
            provider("Coqui TTS")
        }
        
        manageTTSNotification.show(book, chapter, state)
    }
    
    fun onParagraphComplete(index: Int) {
        currentParagraph = index + 1
        manageTTSNotification.updateProgress(currentParagraph, totalParagraphs)
    }
    
    fun onSpeedChange(speed: Float) {
        manageTTSNotification.updateSpeed(speed)
    }
    
    fun pause() {
        isPlaying = false
        manageTTSNotification.updatePlaybackState(isPlaying = false, isPaused = true)
    }
    
    fun resume() {
        isPlaying = true
        manageTTSNotification.updatePlaybackState(isPlaying = true, isPaused = false)
    }
    
    fun stop() {
        isPlaying = false
        manageTTSNotification.hide()
    }
}
