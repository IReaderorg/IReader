package ireader.domain.services.translationService

import ireader.core.log.Log
import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.prefs.TranslationPreferences
import ireader.domain.services.common.ServiceResult
import ireader.domain.services.common.ServiceState
import ireader.domain.services.common.TranslationProgress
import ireader.domain.services.common.TranslationQueueResult
import ireader.domain.services.common.TranslationService
import ireader.domain.services.common.TranslationServiceConstants
import ireader.domain.services.common.TranslationStatus
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.domain.usecases.translation.GetTranslatedChapterUseCase
import ireader.domain.usecases.translation.SaveTranslatedChapterUseCase
import ireader.i18n.LocalizeHelper
import ireader.i18n.asString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    private val stateHolder: TranslationStateHolder = TranslationStateHolder(),
    private val submitTranslationUseCase: ireader.domain.community.SubmitTranslationUseCase? = null,
    private val communityPreferences: ireader.domain.community.CommunityPreferences? = null,
    private val localizeHelper: LocalizeHelper
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
        bypassWarning: Boolean,
        priority: Boolean
    ): ServiceResult<TranslationQueueResult> {
        if (chapterIds.isEmpty()) {
            return ServiceResult.Error("No chapters to translate")
        }
        
        Log.info { "Queueing ${chapterIds.size} chapters for translation (priority=$priority)" }
        
        // For priority requests (single chapter in reader), don't cancel existing translations
        // Just add to front of queue
        val currentBook = stateHolder.currentBookId.value
        if (!priority && currentBook != null && currentBook != bookId && stateHolder.isRunning.value) {
            // Cancel previous translation only for non-priority requests
            cancelAll()
            return ServiceResult.Success(TranslationQueueResult.PreviousTranslationCancelled(currentBook))
        }
        
        // Check rate limit warning (skip for priority/single chapter)
        val threshold = translationPreferences.translationWarningThreshold().get()
        val shouldBypass = bypassWarning || 
            translationPreferences.bypassTranslationWarning().get() ||
            isOfflineEngine(engineId) ||
            priority // Skip warning for priority (single chapter) requests
        
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
        
        // For priority requests, add to existing queue instead of clearing
        if (priority && stateHolder.isRunning.value) {
            // Add to front of queue (priority)
            val newTasks = chapters.map { chapter ->
                TranslationTask(
                    chapterId = chapter.id,
                    bookId = bookId,
                    chapterName = chapter.name,
                    bookName = book.title,
                    needsDownload = chapter.isEmpty()
                )
            }
            // Insert at front of queue
            translationQueue.addAll(0, newTasks)
            
            // Update progress for new chapters (this overwrites any old failed/completed status)
            chapters.forEach { chapter ->
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
            
            // Update total count
            stateHolder.setTotalChapters(stateHolder.totalChapters.value + chapters.size)
            
            Log.info { "Added ${chapters.size} priority chapters to front of queue" }
            return ServiceResult.Success(TranslationQueueResult.Success(chapters.size))
        }
        
        // Normal flow: clear queue and start fresh
        stateHolder.setCurrentBookId(bookId)
        stateHolder.setTotalChapters(chapters.size)
        stateHolder.setCompletedChapters(0)
        
        // IMPORTANT: Clear progress map to remove old entries from previous translations
        // This prevents "Translation Failed" notification from showing due to old failed entries
        stateHolder.setTranslationProgress(emptyMap())
        
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
            
            // Clear progress map after a short delay to allow notification to show completion
            // This prevents old entries from affecting future translations
            scope.launch {
                delay(3000) // Wait 3 seconds for notification to be shown
                // Only clear if no new translation has started
                if (!stateHolder.isRunning.value && translationQueue.isEmpty()) {
                    stateHolder.setTranslationProgress(emptyMap())
                    Log.info { "TranslationServiceImpl: Cleared progress map after completion" }
                }
            }
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
        
        // Translate content with progress tracking
        val translatedContent = translateContentWithProgress(
            content = textContent,
            task = task,
            totalParagraphs = textContent.size
        )
        
        // Save translated content to translatedChapter table (not chapter table)
        saveTranslatedChapter.execute(
            chapter = chapter,
            translatedContent = translatedContent.map { Text(it) },
            sourceLanguage = currentSourceLang,
            targetLanguage = currentTargetLang,
            engineId = currentEngineId
        )
        
        // Submit to community if auto-share is enabled
        submitToCommunityIfEnabled(
            bookId = task.bookId,
            chapter = chapter,
            translatedContent = translatedContent.joinToString("\n\n"),
            sourceLanguage = currentSourceLang,
            targetLanguage = currentTargetLang
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
        
        val content = downloadedContent ?: throw Exception("Failed to download chapter content")
        
        // Save downloaded content to the chapter table before translation
        // This ensures the original content is persisted even if translation fails
        Log.info { "TranslationServiceImpl: Saving downloaded content for chapter ${task.chapterId}" }
        val updatedChapter = chapter.copy(content = content)
        chapterRepository.insertChapter(updatedChapter)
        
        return content
    }

    private suspend fun translateContentWithProgress(
        content: List<String>,
        task: TranslationTask,
        totalParagraphs: Int
    ): List<String> {
        val engine = translationEnginesManager.get()
        val maxChars = engine.maxCharsPerRequest
        val delayMs = if (engine.isOffline) 0L else maxOf(engine.rateLimitDelayMs, 3000L)
        
        // Chunk content based on engine's max character limit
        val chunks = chunkContent(content, maxChars)
        val result = mutableListOf<String>()
        var error: String? = null
        val totalChunks = chunks.size
        var translatedParagraphCount = 0
        
        Log.info { "Translating $totalParagraphs paragraphs in $totalChunks chunks (max $maxChars chars per chunk)" }
        
        for ((index, chunk) in chunks.withIndex()) {
            // Update progress with chunk info
            val progress = (index.toFloat() / totalChunks.toFloat()).coerceIn(0.3f, 0.9f)
            stateHolder.updateChapterProgress(
                task.chapterId,
                TranslationProgress(
                    chapterId = task.chapterId,
                    chapterName = task.chapterName,
                    bookName = task.bookName,
                    status = TranslationStatus.TRANSLATING,
                    progress = progress,
                    currentChunk = index + 1,
                    totalChunks = totalChunks,
                    translatedParagraphs = translatedParagraphCount,
                    totalParagraphs = totalParagraphs
                )
            )
            
            // Apply rate limiting between chunks for online engines
            if (index > 0 && !engine.isOffline) {
                Log.info { "Rate limiting: waiting ${delayMs}ms before next chunk" }
                delay(delayMs)
            }
            
            var chunkResult: List<String>? = null
            var chunkError: String? = null
            
            translationEnginesManager.translateWithContext(
                texts = chunk,
                source = currentSourceLang,
                target = currentTargetLang,
                onProgress = { /* Progress tracking */ },
                onSuccess = { translations ->
                    chunkResult = translations
                },
                onError = { uiText ->
                    chunkError = uiText.asString(localizeHelper)
                }
            )
            
            if (chunkError != null) {
                error = chunkError
                break
            }
            
            chunkResult?.let { 
                result.addAll(it)
                translatedParagraphCount += chunk.size
            }
        }
        
        if (error != null) {
            throw Exception("Translation failed: $error")
        }
        
        return result
    }
    
    // Keep old method for backward compatibility
    private suspend fun translateContent(content: List<String>): List<String> {
        val engine = translationEnginesManager.get()
        val maxChars = engine.maxCharsPerRequest
        val delayMs = if (engine.isOffline) 0L else maxOf(engine.rateLimitDelayMs, 3000L)
        
        val chunks = chunkContent(content, maxChars)
        val result = mutableListOf<String>()
        var error: String? = null
        
        for ((index, chunk) in chunks.withIndex()) {
            if (index > 0 && !engine.isOffline) {
                delay(delayMs)
            }
            
            var chunkResult: List<String>? = null
            var chunkError: String? = null
            
            translationEnginesManager.translateWithContext(
                texts = chunk,
                source = currentSourceLang,
                target = currentTargetLang,
                onProgress = { },
                onSuccess = { translations -> chunkResult = translations },
                onError = { uiText -> chunkError = uiText.asString(localizeHelper) }
            )
            
            if (chunkError != null) {
                error = chunkError
                break
            }
            
            chunkResult?.let { result.addAll(it) }
        }
        
        if (error != null) {
            throw Exception("Translation failed: $error")
        }
        
        return result
    }
    
    /**
     * Chunk content into smaller pieces based on max character limit.
     * Tries to keep paragraphs together when possible.
     */
    private fun chunkContent(content: List<String>, maxChars: Int): List<List<String>> {
        val chunks = mutableListOf<List<String>>()
        var currentChunk = mutableListOf<String>()
        var currentChunkSize = 0
        
        for (text in content) {
            val textSize = text.length
            
            // If single text exceeds max, split it
            if (textSize > maxChars) {
                // First, add current chunk if not empty
                if (currentChunk.isNotEmpty()) {
                    chunks.add(currentChunk.toList())
                    currentChunk = mutableListOf()
                    currentChunkSize = 0
                }
                
                // Split large text into smaller pieces
                val splitTexts = splitLargeText(text, maxChars)
                for (splitText in splitTexts) {
                    chunks.add(listOf(splitText))
                }
            } else if (currentChunkSize + textSize > maxChars) {
                // Current chunk is full, start new one
                if (currentChunk.isNotEmpty()) {
                    chunks.add(currentChunk.toList())
                }
                currentChunk = mutableListOf(text)
                currentChunkSize = textSize
            } else {
                // Add to current chunk
                currentChunk.add(text)
                currentChunkSize += textSize
            }
        }
        
        // Add remaining chunk
        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk.toList())
        }
        
        return chunks
    }
    
    /**
     * Split a large text into smaller pieces, trying to break at sentence boundaries.
     */
    private fun splitLargeText(text: String, maxChars: Int): List<String> {
        if (text.length <= maxChars) return listOf(text)
        
        val result = mutableListOf<String>()
        var remaining = text
        
        while (remaining.length > maxChars) {
            // Try to find a good break point (sentence end)
            var breakPoint = remaining.lastIndexOf(". ", maxChars)
            if (breakPoint == -1) breakPoint = remaining.lastIndexOf("。", maxChars)
            if (breakPoint == -1) breakPoint = remaining.lastIndexOf("! ", maxChars)
            if (breakPoint == -1) breakPoint = remaining.lastIndexOf("? ", maxChars)
            if (breakPoint == -1) breakPoint = remaining.lastIndexOf("\n", maxChars)
            
            // If no good break point, just break at max chars
            if (breakPoint == -1 || breakPoint < maxChars / 2) {
                breakPoint = maxChars
            } else {
                breakPoint += 1 // Include the punctuation
            }
            
            result.add(remaining.substring(0, breakPoint).trim())
            remaining = remaining.substring(breakPoint).trim()
        }
        
        if (remaining.isNotEmpty()) {
            result.add(remaining)
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
    
    /**
     * Submit translation to community if auto-share is enabled.
     * This is a fire-and-forget operation - failures are logged but don't affect the main translation flow.
     */
    private suspend fun submitToCommunityIfEnabled(
        bookId: Long,
        chapter: ireader.domain.models.entities.Chapter,
        translatedContent: String,
        sourceLanguage: String,
        targetLanguage: String
    ) {
        // Check if auto-share is enabled
        val autoShare = communityPreferences?.autoShareTranslations()?.get() ?: false
        if (!autoShare) {
            return
        }
        
        // Check if we have the use case
        val useCase = submitTranslationUseCase ?: return
        
        try {
            val book = bookRepository.findBookById(bookId) ?: return
            
            Log.info { "TranslationServiceImpl: Submitting translation to community for chapter ${chapter.name}" }
            
            val result = useCase.submitChapter(
                book = book,
                chapter = chapter,
                translatedContent = translatedContent,
                targetLanguage = targetLanguage,
                sourceLanguage = sourceLanguage
            )
            
            if (result.isSuccess) {
                Log.info { "TranslationServiceImpl: Successfully submitted translation to community" }
            } else {
                Log.warn { "TranslationServiceImpl: Failed to submit translation to community: ${result.exceptionOrNull()?.message}" }
            }
        } catch (e: Exception) {
            // Don't fail the main translation if community submission fails
            Log.warn { "TranslationServiceImpl: Error submitting to community: ${e.message}" }
        }
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
