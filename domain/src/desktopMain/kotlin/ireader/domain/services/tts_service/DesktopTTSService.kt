package ireader.domain.services.tts_service

import ireader.core.log.Log
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.remote.RemoteUseCases
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

/**
 * Desktop TTS Service
 * Uses a simple word-by-word simulation for TTS
 * For production, consider integrating with FreeTTS, MaryTTS, or system TTS
 */
class DesktopTTSService : KoinComponent {
    private val bookRepo: BookRepository by inject()
    private val chapterRepo: ChapterRepository by inject()
    private val chapterUseCase: LocalGetChapterUseCase by inject()
    private val remoteUseCases: RemoteUseCases by inject()
    private val extensions: CatalogStore by inject()
    private val readerPreferences: ReaderPreferences by inject()
    private val appPrefs: AppPreferences by inject()

    lateinit var state: DesktopTTSState
    private var serviceJob: Job? = null
    private var speechJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    companion object {
        const val TTS_SERVICE_NAME = "DESKTOP_TTS_SERVICE"
        const val ACTION_STOP = "actionStop"
        const val ACTION_PAUSE = "actionPause"
        const val ACTION_PLAY = "actionPlay"
        const val ACTION_NEXT = "actionNext"
        const val ACTION_PREVIOUS = "actionPrevious"
        const val ACTION_SKIP_NEXT = "actionSkipNext"
        const val ACTION_SKIP_PREV = "actionSkipPrev"
        const val ACTION_NEXT_PAR = "actionNextPar"
        const val ACTION_PREV_PAR = "actionPrevPar"
    }

    fun initialize() {
        state = DesktopTTSState()
        readPrefs()
    }

    private fun readPrefs() {
        serviceScope.launch {
            state.autoNextChapter = readerPreferences.readerAutoNext().get()
            state.currentLanguage = readerPreferences.speechLanguage().get()
            state.currentVoice = appPrefs.speechVoice().get()
            state.speechSpeed = readerPreferences.speechRate().get()
            state.pitch = readerPreferences.speechPitch().get()
            state.sleepTime = readerPreferences.sleepTime().get()
            state.sleepMode = readerPreferences.sleepMode().get()
        }
        
        // Listen to preference changes
        serviceScope.launch {
            readerPreferences.readerAutoNext().changes().collect {
                state.autoNextChapter = it
            }
        }
        serviceScope.launch {
            readerPreferences.speechLanguage().changes().collect {
                state.currentLanguage = it
            }
        }
        serviceScope.launch {
            appPrefs.speechVoice().changes().collect {
                state.currentVoice = it
            }
        }
        serviceScope.launch {
            readerPreferences.speechPitch().changes().collect {
                state.pitch = it
            }
        }
        serviceScope.launch {
            readerPreferences.speechRate().changes().collect {
                state.speechSpeed = it
            }
        }
        serviceScope.launch {
            readerPreferences.sleepTime().changes().collect {
                state.sleepTime = it
            }
        }
        serviceScope.launch {
            readerPreferences.sleepMode().changes().collect {
                state.sleepMode = it
            }
        }
    }

    suspend fun startReading(bookId: Long, chapterId: Long) {
        val book = bookRepo.findBookById(bookId)
        val chapter = chapterRepo.findChapterById(chapterId)
        val chapters = chapterRepo.findChaptersByBookId(bookId)
        val source = book?.sourceId?.let { extensions.get(it) }

        if (chapter != null && source != null && book != null) {
            state.ttsBook = book
            state.ttsChapter = chapter
            state.ttsChapters = chapters
            state.ttsCatalog = source
            state.currentReadingParagraph = 0
            startService(ACTION_PLAY)
        }
    }

    fun startService(action: String) {
        serviceJob = serviceScope.launch {
            try {
                when (action) {
                    ACTION_STOP -> {
                        stopReading()
                    }
                    ACTION_PAUSE -> {
                        pauseReading()
                    }
                    ACTION_PLAY -> {
                        playReading()
                    }
                    ACTION_SKIP_NEXT -> {
                        skipToNextChapter()
                    }
                    ACTION_SKIP_PREV -> {
                        skipToPreviousChapter()
                    }
                    ACTION_NEXT_PAR -> {
                        nextParagraph()
                    }
                    ACTION_PREV_PAR -> {
                        previousParagraph()
                    }
                }
            } catch (e: Exception) {
                Log.error { "Desktop TTS error" }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun playReading() {
        state.isPlaying = true
        state.startTime = kotlin.time.Clock.System.now()
        readText()
    }

    private fun pauseReading() {
        state.isPlaying = false
        speechJob?.cancel()
    }

    private fun stopReading() {
        state.isPlaying = false
        speechJob?.cancel()
        state.currentReadingParagraph = 0
    }

    private suspend fun skipToNextChapter() {
        val chapter = state.ttsChapter ?: return
        val chapters = state.ttsChapters
        val index = getChapterIndex(chapter, chapters)
        
        if (index < chapters.lastIndex) {
            val nextChapter = chapters[index + 1]
            loadChapter(nextChapter.id)
            state.currentReadingParagraph = 0
            if (state.isPlaying) {
                readText()
            }
        }
    }

    private suspend fun skipToPreviousChapter() {
        val chapter = state.ttsChapter ?: return
        val chapters = state.ttsChapters
        val index = getChapterIndex(chapter, chapters)
        
        if (index > 0) {
            val prevChapter = chapters[index - 1]
            loadChapter(prevChapter.id)
            state.currentReadingParagraph = 0
            if (state.isPlaying) {
                readText()
            }
        }
    }

    private fun nextParagraph() {
        state.ttsContent?.value?.let { content ->
            if (state.currentReadingParagraph < content.lastIndex) {
                state.currentReadingParagraph += 1
                if (state.isPlaying) {
                    serviceScope.launch { readText() }
                }
            }
        }
    }

    private fun previousParagraph() {
        state.ttsContent?.value?.let { content ->
            if (state.currentReadingParagraph > 0) {
                state.currentReadingParagraph -= 1
                if (state.isPlaying) {
                    serviceScope.launch { readText() }
                }
            }
        }
    }

    private suspend fun readText() {
        val content = state.ttsContent?.value ?: return
        val chapter = state.ttsChapter ?: return
        
        if (state.currentReadingParagraph >= content.size) {
            // End of chapter
            if (state.autoNextChapter) {
                skipToNextChapter()
            } else {
                stopReading()
            }
            return
        }

        val text = content[state.currentReadingParagraph]
        state.utteranceId = state.currentReadingParagraph.toString()

        // Simulate TTS by calculating reading time
        speechJob = serviceScope.launch {
            try {
                // Calculate reading time based on word count and speech rate
                val words = text.split("\\s+".toRegex())
                val wordsPerMinute = 150 * state.speechSpeed // Base reading speed
                val readingTimeMs = (words.size / wordsPerMinute * 60 * 1000).toLong()
                
                Log.debug { "Reading paragraph ${state.currentReadingParagraph}: $text" }
                Log.debug { "Estimated reading time: ${readingTimeMs}ms for ${words.size} words" }
                
                delay(readingTimeMs)
                
                // Check sleep time
                checkSleepTime()
                
                if (state.isPlaying) {
                    // Move to next paragraph
                    if (state.currentReadingParagraph < content.lastIndex) {
                        state.currentReadingParagraph += 1
                        readText()
                    } else {
                        // End of chapter
                        if (state.autoNextChapter) {
                            skipToNextChapter()
                        } else {
                            stopReading()
                        }
                    }
                }
            } catch (e: CancellationException) {
                // Speech was cancelled
                Log.debug { "Speech cancelled" }
            } catch (e: Exception) {
                Log.error { "Error during speech" }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun checkSleepTime() {
        val lastCheckPref = state.startTime
        val currentSleepTime = state.sleepTime.minutes
        val now = kotlin.time.Clock.System.now()
        if (lastCheckPref != null && now - lastCheckPref > currentSleepTime && state.sleepMode) {
            stopReading()
        }
    }

    private suspend fun loadChapter(chapterId: Long) {
        val localChapter = chapterRepo.findChapterById(chapterId)
        val source = state.ttsCatalog ?: return

        if (localChapter != null && !localChapter.isEmpty()) {
            state.ttsChapter = localChapter
            chapterUseCase.updateLastReadTime(localChapter, updateDateFetched = false)
        } else if (localChapter != null) {
            remoteUseCases.getRemoteReadingContent(
                chapter = localChapter,
                source,
                onSuccess = { result ->
                    if (result.content.joinToString().length > 1) {
                        state.ttsChapter = result
                        chapterUseCase.updateLastReadTime(result, updateDateFetched = true)
                        chapterRepo.findChaptersByBookId(result.bookId).let { res ->
                            state.ttsChapters = res
                        }
                    }
                },
                onError = {
                    Log.error { "Failed to load chapter: $it" }
                }
            )
        }
    }

    private fun getChapterIndex(chapter: Chapter, chapters: List<Chapter>): Int {
        val chaptersIds = chapters.map { it.name }
        val index = chaptersIds.indexOfFirst { it == chapter.name }
        return if (index != -1) index else throw Exception("Invalid chapter")
    }

    fun shutdown() {
        stopReading()
        serviceJob?.cancel()
        speechJob?.cancel()
    }
}
