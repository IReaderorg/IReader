package ireader.domain.services.translationService

import ireader.core.log.Log
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.prefs.TranslationPreferences
import ireader.domain.services.common.*
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.domain.usecases.translation.SaveTranslatedChapterUseCase
import ireader.domain.usecases.translation.GetTranslatedChapterUseCase
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.core.source.model.Page
import ireader.core.source.model.Text
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.ExperimentalTime

/**
 * Implementation of TranslationService for mass chapter translation.
 * Handles rate limiting, progress tracking, and foreground service notifications.
 */
class TranslationServiceImpl(
    private val chapterRepository: ChapterRepository,
    private val bookRepository: BookRepository,
    private val translationEnginesManager: TranslationEnginesManager,
    private val saveTranslatedChapter: SaveTranslatedChapterUseCase,
    private val getTranslatedChapter: GetTranslatedChapterUseCase,
    private val translationPreferences: TranslationPreferences,
    private val readerPreferences: ReaderPreferences,
    private val remoteUseCases: RemoteUseCases,
    private val getLocalCatalog: GetLocalCatalog,
    private val stateHolder: TranslationStateHolder = TranslationStateHolder()
) : TranslationService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var translationJob: Job? = null
    
    private val _state = MutableStateFlow(ServiceState.IDLE)
    override val state: StateFlow<ServiceState> = _state.asStateFlow()
    
    override val translationProgress: StateFlow<Map<Long, TranslationProgress>> = stateHolder.translationProgress
    override val currentBookId: StateFlow<Long?> = stateHolder.currentBookId
    
    // Queue of chapters to translate
    private val translationQueue = mutableListOf<TranslationTask>()
    private var currentSourceLang: String = "en"
    private var currentTargetLang: String = "en"
    private var currentEngineId: Long = -1L
    
    // Rate limiting state
    private var requestCount = 0
    private var lastRequestTime = 0L
    
    // Offline engines that don't need rate limiting
    private val offlineEngineIds = setOf(
        0L,  // Google ML Kit (offline)
        4L,  // LibreTranslate (can be self-hosted)
        5L,  // Ollama (local)
    )
    
    // Engines that require rate limiting (web-based AI)
    private val rateLimitedEngineIds = setOf(
        2L,  // OpenAI
        3L,  // DeepSeek API
        6L,  // ChatGPT WebView
        7L,  // DeepSeek WebView
        8L,  // Gemini API
    )

    override suspend fun initialize() {
        Log.info { "TranslationService initialized" }
    }

    override suspend fun cleanup() {
        translationJob?.cancel()
        stateHolder.reset()
        translationQueue.clear()
        _state.value = ServiceState.IDLE
        Log.info { "TranslationService cleaned up" }
    }
    
    override suspend fun start() {
        _state.value = ServiceState.RUNNING
    }
    
    override suspend fun stop() {
        _state.value = ServiceState.STOPPED
        translationJob?.cancel()
    }
    
    override fun isRunning(): Boolean {
        return _state.value == ServiceState.RUNNING
    }

    override suspend fun queueChapters(
        bookId: Long,
        chapterIds: List<Long>,
        sourceLanguage: String,
        targetLanguage: String,
        engineId: Long,
        bypassWarning: Boolean
    ): ServiceResult<TranslationQueueResult> {
        if (chapterIds.isEmpty()) {
            return ServiceResult.Error("No chapters to translate")
        }
        
        // Check if another book is being translated
        val currentBook = stateHolder.currentBookId.value
        if (currentBook != null && currentBook != bookId && stateHolder.isRunning.value) {
            // Cancel previous translation
            cancelAll()
            return ServiceResult.Success(TranslationQueueResult.PreviousTranslationCancelled(currentBook))
        }
        
        // Check rate limit warning
        val threshold = translationPreferences.translationWarningThreshold().get()
        val shouldBypass = bypassWarning || 
            translationPreferences.bypassTranslationWarning().get() ||
            isOfflineEngine(engineId)
        
        if (!shouldBypass && chapterIds.size >= threshold && requiresRateLimiting(engineId)) {
            val delayMs = translationPreferences.translationRateLimitDelayMs().get()
            val estimatedTime = chapterIds.size * delayMs
            return ServiceResult.Success(
                TranslationQueueResult.RateLimitWarning(
                    chapterCount = chapterIds.size,
                    estimatedTime = estimatedTime,
                    message = "Translating ${chapterIds.size} chapters may take approximately ${estimatedTime / 60000} minutes and could exhaust API credits or result in IP blocking."
                )
            )
        }
        
        // Get book and chapter info
        val book = bookRepository.findBookById(bookId)
            ?: return ServiceResult.Error("Book not found")
        val chapters = chapterRepository.findChaptersByBookId(bookId)
            .filter { it.id in chapterIds }
        
        if (chapters.isEmpty()) {
            return ServiceResult.Error("No valid chapters found")
        }
        
        // Setup translation state
        currentSourceLang = sourceLanguage
        currentTargetLang = targetLanguage
        currentEngineId = engineId
        stateHolder.setCurrentBookId(bookId)
        stateHolder.setTotalChapters(chapters.size)
        stateHolder.setCompletedChapters(0)
        
        // Create translation tasks
        translationQueue.clear()
        chapters.forEach { chapter ->
            val task = TranslationTask(
                chapterId = chapter.id,
                bookId = bookId,
                chapterName = chapter.name,
                bookName = book.title,
                needsDownload = chapter.isEmpty()
            )
            translationQueue.add(task)
            stateHolder.updateChapterProgress(
                chapter.id,
                TranslationProgress(
                    chapterId = chapter.id,
                    chapterName = chapter.name,
                    bookName = book.title,
                    status = TranslationStatus.QUEUED
                )
            )
        }
        
        // Start translation
        startTranslation()
        
        return ServiceResult.Success(TranslationQueueResult.Success(chapters.size))
    }

    private fun startTranslation() {
        if (translationJob?.isActive == true) return
        
        _state.value = ServiceState.RUNNING
        stateHolder.setRunning(true)
        
        translationJob = scope.launch {
            try {
                processQueue()
            } catch (e: CancellationException) {
                Log.info { "Translation cancelled" }
            } catch (e: Exception) {
                Log.error("Translation error", e)
            } finally {
                _state.value = ServiceState.IDLE
                stateHolder.setRunning(false)
            }
        }
    }

    private suspend fun processQueue() {
        val delayMs = translationPreferences.translationRateLimitDelayMs().get()
        val burstSize = TranslationServiceConstants.RATE_LIMIT_BURST_SIZE
        
        while (translationQueue.isNotEmpty() && !stateHolder.isPaused.value) {
            val task = translationQueue.removeFirstOrNull() ?: break
            
            try {
                // Apply rate limiting for web-based engines
                if (requiresRateLimiting(currentEngineId)) {
                    applyRateLimit(delayMs, burstSize)
                }
                
                // Process the chapter
                processChapter(task)
                
                stateHolder.incrementCompleted()
                
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.error("Failed to translate chapter ${task.chapterId}", e)
                stateHolder.updateChapterProgress(
                    task.chapterId,
                    TranslationProgress(
                        chapterId = task.chapterId,
                        chapterName = task.chapterName,
                        bookName = task.bookName,
                        status = TranslationStatus.FAILED,
                        errorMessage = e.message
                    )
                )
            }
        }
        
        // Reset state when done
        if (translationQueue.isEmpty()) {
            stateHolder.setCurrentBookId(null)
        }
    }

    private suspend fun processChapter(task: TranslationTask) {
        var chapterContent: List<Page>? = null
        
        // Update status to downloading if needed
        if (task.needsDownload) {
            stateHolder.updateChapterProgress(
                task.chapterId,
                TranslationProgress(
                    chapterId = task.chapterId,
                    chapterName = task.chapterName,
                    bookName = task.bookName,
                    status = TranslationStatus.DOWNLOADING_CONTENT,
                    progress = 0.1f
                )
            )
            
            // Download chapter content (returns the content directly)
            chapterContent = downloadChapterContent(task)
        }
        
        // Update status to translating
        stateHolder.updateChapterProgress(
            task.chapterId,
            TranslationProgress(
                chapterId = task.chapterId,
                chapterName = task.chapterName,
                bookName = task.bookName,
                status = TranslationStatus.TRANSLATING,
                progress = 0.3f
            )
        )
        
        // Get chapter for metadata
        val chapter = chapterRepository.findChapterById(task.chapterId)
            ?: throw Exception("Chapter not found")
        
        // Use downloaded content or existing chapter content
        val contentToTranslate = chapterContent ?: chapter.content
        
        if (contentToTranslate.isEmpty()) {
            throw Exception("Chapter has no content to translate")
        }
        
        // Extract text content from pages
        val textContent = contentToTranslate.mapNotNull { page ->
            when (page) {
                is Text -> page.text
                else -> null
            }
        }
        
        if (textContent.isEmpty()) {
            throw Exception("Chapter has no text content to translate")
        }
        
        // Translate content
        val translatedContent = translateContent(textContent)
        
        // Update progress
        stateHolder.updateChapterProgress(
            task.chapterId,
            TranslationProgress(
                chapterId = task.chapterId,
                chapterName = task.chapterName,
                bookName = task.bookName,
                status = TranslationStatus.TRANSLATING,
                progress = 0.8f
            )
        )
        
        // Save translated content to translatedChapter table (not chapter table)
        saveTranslatedChapter.execute(
            chapter = chapter,
            translatedContent = translatedContent.map { Text(it) },
            sourceLanguage = currentSourceLang,
            targetLanguage = currentTargetLang,
            engineId = currentEngineId
        )
        
        // Mark as completed
        stateHolder.updateChapterProgress(
            task.chapterId,
            TranslationProgress(
                chapterId = task.chapterId,
                chapterName = task.chapterName,
                bookName = task.bookName,
                status = TranslationStatus.COMPLETED,
                progress = 1f
            )
        )
    }

    private suspend fun downloadChapterContent(task: TranslationTask): List<Page> {
        val book = bookRepository.findBookById(task.bookId)
            ?: throw Exception("Book not found")
        val chapter = chapterRepository.findChapterById(task.chapterId)
            ?: throw Exception("Chapter not found")
        val catalog = getLocalCatalog.get(book.sourceId)
            ?: throw Exception("Source not found")
        
        var downloadedContent: List<Page>? = null
        var downloadError: String? = null
        
        remoteUseCases.getRemoteReadingContent(
            chapter = chapter,
            catalog = catalog,
            onSuccess = { downloadedChapter ->
                // Extract content from the returned chapter
                downloadedContent = downloadedChapter.content
            },
            onError = { error ->
                downloadError = error?.toString()
            }
        )
        
        if (downloadError != null) {
            throw Exception("Failed to download chapter: $downloadError")
        }
        
        // Return downloaded content for translation
        // The original chapter content is not modified - translations go to translatedChapter table
        return downloadedContent ?: throw Exception("Failed to download chapter content")
    }

    private suspend fun translateContent(content: List<String>): List<String> {
        val result = mutableListOf<String>()
        var error: String? = null
        
        translationEnginesManager.translateWithContext(
            texts = content,
            source = currentSourceLang,
            target = currentTargetLang,
            onProgress = { /* Progress tracking */ },
            onSuccess = { translations ->
                result.addAll(translations)
            },
            onError = { uiText ->
                error = uiText.toString()
            }
        )
        
        if (error != null) {
            throw Exception("Translation failed: $error")
        }
        
        return result
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun applyRateLimit(delayMs: Long, burstSize: Int) {
        requestCount++
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        
        if (requestCount > burstSize) {
            val timeSinceLastRequest = now - lastRequestTime
            if (timeSinceLastRequest < delayMs) {
                val waitTime = delayMs - timeSinceLastRequest
                delay(waitTime)
            }
        }
        
        lastRequestTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
    }

    override suspend fun pause() {
        stateHolder.setPaused(true)
        _state.value = ServiceState.PAUSED
    }

    override suspend fun resume() {
        stateHolder.setPaused(false)
        if (translationQueue.isNotEmpty()) {
            startTranslation()
        }
    }

    override suspend fun cancelTranslation(chapterId: Long): ServiceResult<Unit> {
        translationQueue.removeAll { it.chapterId == chapterId }
        stateHolder.updateChapterProgress(
            chapterId,
            stateHolder.translationProgress.value[chapterId]?.copy(
                status = TranslationStatus.CANCELLED
            ) ?: TranslationProgress(chapterId = chapterId, status = TranslationStatus.CANCELLED)
        )
        return ServiceResult.Success(Unit)
    }

    override suspend fun cancelAll(): ServiceResult<Unit> {
        translationJob?.cancel()
        translationQueue.forEach { task ->
            stateHolder.updateChapterProgress(
                task.chapterId,
                TranslationProgress(
                    chapterId = task.chapterId,
                    chapterName = task.chapterName,
                    bookName = task.bookName,
                    status = TranslationStatus.CANCELLED
                )
            )
        }
        translationQueue.clear()
        stateHolder.reset()
        _state.value = ServiceState.IDLE
        return ServiceResult.Success(Unit)
    }

    override suspend fun retryTranslation(chapterId: Long): ServiceResult<Unit> {
        val progress = stateHolder.translationProgress.value[chapterId]
            ?: return ServiceResult.Error("Chapter not found in translation queue")
        
        if (progress.status != TranslationStatus.FAILED) {
            return ServiceResult.Error("Chapter is not in failed state")
        }
        
        val task = TranslationTask(
            chapterId = chapterId,
            bookId = stateHolder.currentBookId.value ?: return ServiceResult.Error("No active translation"),
            chapterName = progress.chapterName,
            bookName = progress.bookName,
            needsDownload = false
        )
        
        translationQueue.add(task)
        stateHolder.updateChapterProgress(
            chapterId,
            progress.copy(
                status = TranslationStatus.QUEUED,
                retryCount = progress.retryCount + 1
            )
        )
        
        if (!stateHolder.isRunning.value) {
            startTranslation()
        }
        
        return ServiceResult.Success(Unit)
    }

    override fun getTranslationStatus(chapterId: Long): TranslationStatus? {
        return stateHolder.translationProgress.value[chapterId]?.status
    }

    override fun requiresRateLimiting(engineId: Long): Boolean {
        return engineId in rateLimitedEngineIds
    }

    override fun isOfflineEngine(engineId: Long): Boolean {
        return engineId in offlineEngineIds
    }
}

/**
 * Internal task representation for translation queue
 */
private data class TranslationTask(
    val chapterId: Long,
    val bookId: Long,
    val chapterName: String,
    val bookName: String,
    val needsDownload: Boolean
)
