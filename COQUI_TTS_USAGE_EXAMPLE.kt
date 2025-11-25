/**
 * Comprehensive Coqui TTS Usage Example
 * 
 * This file demonstrates how to use the CoquiTTSService with all features:
 * - Auto next chapter
 * - Auto load next 3 paragraphs
 * - Auto continue to end of chapter
 * - Android notifications (via TTSService integration)
 */

package ireader.examples

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.tts.CoquiTTSService
import kotlinx.coroutines.launch

class CoquiTTSUsageExample(
    private val context: Context,
    private val appPrefs: AppPreferences,
    private val readerPrefs: ReaderPreferences
) : ViewModel() {

    private var coquiService: CoquiTTSService? = null
    private var currentChapterIndex = 0
    private var chapters: List<Chapter> = emptyList()

    /**
     * Example 1: Basic Usage - Read a Single Chapter
     */
    fun example1_BasicUsage(chapter: Chapter) {
        viewModelScope.launch {
            // Initialize service
            val service = CoquiTTSService(
                context = context,
                spaceUrl = appPrefs.coquiSpaceUrl().get(),
                apiKey = appPrefs.coquiApiKey().get()
            )

            // Split chapter into paragraphs
            val paragraphs = chapter.content.split("\n\n").filter { it.isNotBlank() }

            // Start reading
            service.startReading(
                paragraphs = paragraphs,
                startIndex = 0,
                speed = 1.0f,
                autoNext = true,
                onParagraphComplete = { index ->
                    println("✅ Completed paragraph $index")
                },
                onChapterComplete = {
                    println("✅ Chapter complete!")
                }
            )

            coquiService = service
        }
    }

    /**
     * Example 2: Auto Next Chapter - Read Multiple Chapters
     */
    fun example2_AutoNextChapter(chapters: List<Chapter>) {
        this.chapters = chapters
        this.currentChapterIndex = 0

        viewModelScope.launch {
            // Initialize service
            val service = CoquiTTSService(
                context = context,
                spaceUrl = appPrefs.coquiSpaceUrl().get(),
                apiKey = appPrefs.coquiApiKey().get()
            )

            // Start reading first chapter
            readChapter(service, currentChapterIndex)

            coquiService = service
        }
    }

    private fun readChapter(service: CoquiTTSService, chapterIndex: Int) {
        if (chapterIndex >= chapters.size) {
            println("✅ All chapters complete!")
            return
        }

        val chapter = chapters[chapterIndex]
        val paragraphs = chapter.content.split("\n\n").filter { it.isNotBlank() }

        viewModelScope.launch {
            service.startReading(
                paragraphs = paragraphs,
                startIndex = 0,
                speed = readerPrefs.speechRate().get(),
                autoNext = true,
                onParagraphComplete = { index ->
                    println("✅ Chapter $chapterIndex - Paragraph $index complete")
                },
                onChapterComplete = {
                    println("✅ Chapter $chapterIndex complete!")

                    // Auto-advance to next chapter if enabled
                    if (readerPrefs.readerAutoNext().get()) {
                        currentChapterIndex++
                        readChapter(service, currentChapterIndex)
                    }
                }
            )
        }
    }

    /**
     * Example 3: Playback Controls
     */
    fun example3_PlaybackControls() {
        val service = coquiService ?: return

        // Pause reading
        service.pauseReading()
        println("⏸️ Paused")

        // Resume reading
        val chapter = chapters[currentChapterIndex]
        val paragraphs = chapter.content.split("\n\n").filter { it.isNotBlank() }
        service.resumeReading(
            paragraphs = paragraphs,
            speed = readerPrefs.speechRate().get(),
            autoNext = true
        )
        println("▶️ Resumed")

        // Stop reading
        service.stopReading()
        println("⏹️ Stopped")
    }

    /**
     * Example 4: Seek to Specific Paragraph
     */
    fun example4_SeekToParagraph(paragraphIndex: Int) {
        val service = coquiService ?: return
        val chapter = chapters[currentChapterIndex]
        val paragraphs = chapter.content.split("\n\n").filter { it.isNotBlank() }

        service.seekToParagraph(
            paragraphs = paragraphs,
            index = paragraphIndex,
            speed = readerPrefs.speechRate().get(),
            autoNext = true
        )
        println("⏩ Seeked to paragraph $paragraphIndex")
    }

    /**
     * Example 5: Speed Control
     */
    fun example5_SpeedControl(speed: Float) {
        val service = coquiService ?: return
        val chapter = chapters[currentChapterIndex]
        val paragraphs = chapter.content.split("\n\n").filter { it.isNotBlank() }

        // Stop current playback
        service.stopReading()

        // Restart with new speed
        viewModelScope.launch {
            service.startReading(
                paragraphs = paragraphs,
                startIndex = service.getCurrentParagraphIndex(),
                speed = speed,
                autoNext = true,
                onParagraphComplete = { index ->
                    println("✅ Paragraph $index complete (speed: $speed)")
                },
                onChapterComplete = {
                    println("✅ Chapter complete!")
                }
            )
        }
    }

    /**
     * Example 6: Check Playback State
     */
    fun example6_CheckState() {
        val service = coquiService ?: return

        val isPlaying = service.isCurrentlyPlaying()
        val currentParagraph = service.getCurrentParagraphIndex()

        println("🎵 Is Playing: $isPlaying")
        println("📄 Current Paragraph: $currentParagraph")
    }

    /**
     * Example 7: Complete Reading Flow with UI Updates
     */
    fun example7_CompleteFlow(chapters: List<Chapter>) {
        this.chapters = chapters
        this.currentChapterIndex = 0

        viewModelScope.launch {
            // Initialize service
            val service = CoquiTTSService(
                context = context,
                spaceUrl = appPrefs.coquiSpaceUrl().get(),
                apiKey = appPrefs.coquiApiKey().get()
            )

            // Start reading with full callbacks
            readChapterWithUIUpdates(service, currentChapterIndex)

            coquiService = service
        }
    }

    private fun readChapterWithUIUpdates(service: CoquiTTSService, chapterIndex: Int) {
        if (chapterIndex >= chapters.size) {
            println("✅ All chapters complete!")
            // Update UI: Show completion message
            return
        }

        val chapter = chapters[chapterIndex]
        val paragraphs = chapter.content.split("\n\n").filter { it.isNotBlank() }

        println("📖 Reading Chapter ${chapterIndex + 1}/${chapters.size}: ${chapter.name}")

        viewModelScope.launch {
            service.startReading(
                paragraphs = paragraphs,
                startIndex = 0,
                speed = readerPrefs.speechRate().get(),
                autoNext = readerPrefs.readerAutoNext().get(),
                onParagraphComplete = { index ->
                    // Update UI: Highlight current paragraph
                    println("📄 Paragraph ${index + 1}/${paragraphs.size}")
                    
                    // Update progress
                    val progress = ((index + 1).toFloat() / paragraphs.size * 100).toInt()
                    println("📊 Progress: $progress%")
                },
                onChapterComplete = {
                    println("✅ Chapter ${chapterIndex + 1} complete!")

                    // Auto-advance to next chapter if enabled
                    if (readerPrefs.readerAutoNext().get() && chapterIndex + 1 < chapters.size) {
                        println("⏭️ Auto-advancing to next chapter...")
                        currentChapterIndex++
                        readChapterWithUIUpdates(service, currentChapterIndex)
                    } else {
                        println("🏁 Reading session complete!")
                        // Update UI: Show completion
                    }
                }
            )
        }
    }

    /**
     * Example 8: Error Handling
     */
    fun example8_ErrorHandling(chapter: Chapter) {
        viewModelScope.launch {
            try {
                // Check if service is available
                val service = CoquiTTSService(
                    context = context,
                    spaceUrl = appPrefs.coquiSpaceUrl().get(),
                    apiKey = appPrefs.coquiApiKey().get()
                )

                val isAvailable = service.isAvailable()
                if (!isAvailable) {
                    println("❌ Coqui TTS service is not available")
                    // Update UI: Show error message
                    return@launch
                }

                // Get available voices
                val voicesResult = service.getAvailableVoices()
                voicesResult.onSuccess { voices ->
                    println("✅ Available voices: ${voices.size}")
                    voices.forEach { voice ->
                        println("  - ${voice.name} (${voice.language})")
                    }
                }.onFailure { error ->
                    println("❌ Failed to get voices: ${error.message}")
                }

                // Start reading with error handling
                val paragraphs = chapter.content.split("\n\n").filter { it.isNotBlank() }
                service.startReading(
                    paragraphs = paragraphs,
                    startIndex = 0,
                    speed = 1.0f,
                    autoNext = true,
                    onParagraphComplete = { index ->
                        println("✅ Paragraph $index complete")
                    },
                    onChapterComplete = {
                        println("✅ Chapter complete!")
                    }
                )

                coquiService = service

            } catch (e: Exception) {
                println("❌ Error: ${e.message}")
                // Update UI: Show error message
            }
        }
    }

    /**
     * Example 9: Cleanup
     */
    fun example9_Cleanup() {
        coquiService?.cleanup()
        coquiService = null
        println("🧹 Cleaned up Coqui TTS service")
    }

    /**
     * Example 10: Integration with TTSService (Notifications)
     * 
     * Note: This is handled automatically when CoquiTTSService is used
     * within the TTSService. The TTSService provides:
     * - Notification controls
     * - MediaSession integration
     * - Lock screen controls
     * - Audio focus management
     * - Background playback
     */
    fun example10_NotificationIntegration() {
        println("""
            📱 Notification Integration:
            
            When CoquiTTSService is used within TTSService, you automatically get:
            
            ✅ Notification Controls:
               - Play/Pause button
               - Stop button
               - Next chapter button
               - Previous chapter button
            
            ✅ Lock Screen Controls:
               - Media controls on lock screen
               - Album art (book cover)
               - Chapter title
               - Progress indicator
            
            ✅ Media Buttons:
               - Headphone play/pause button
               - Bluetooth media controls
               - Android Auto integration
            
            ✅ Background Playback:
               - Continues playing when app is in background
               - Survives screen off
               - Proper audio focus handling
            
            This is all handled automatically by the existing TTSService infrastructure!
        """.trimIndent())
    }

    /**
     * Cleanup when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        coquiService?.cleanup()
    }
}

/**
 * Example Usage in Activity/Fragment
 */
fun exampleUsageInActivity() {
    // Assuming you have access to the ViewModel
    val viewModel: CoquiTTSUsageExample = TODO("Get ViewModel")

    // Example 1: Read a single chapter
    val chapter = Chapter(
        id = 1,
        bookId = 1,
        name = "Chapter 1",
        content = """
            This is the first paragraph of the chapter.
            
            This is the second paragraph of the chapter.
            
            This is the third paragraph of the chapter.
        """.trimIndent()
    )
    viewModel.example1_BasicUsage(chapter)

    // Example 2: Read multiple chapters with auto-next
    val chapters = listOf(
        Chapter(1, 1, "Chapter 1", "Content 1..."),
        Chapter(2, 1, "Chapter 2", "Content 2..."),
        Chapter(3, 1, "Chapter 3", "Content 3...")
    )
    viewModel.example2_AutoNextChapter(chapters)

    // Example 3: Control playback
    viewModel.example3_PlaybackControls()

    // Example 4: Seek to paragraph 5
    viewModel.example4_SeekToParagraph(5)

    // Example 5: Change speed to 1.5x
    viewModel.example5_SpeedControl(1.5f)

    // Example 6: Check current state
    viewModel.example6_CheckState()

    // Example 7: Complete reading flow
    viewModel.example7_CompleteFlow(chapters)

    // Example 8: Error handling
    viewModel.example8_ErrorHandling(chapter)

    // Example 9: Cleanup
    viewModel.example9_Cleanup()
}
