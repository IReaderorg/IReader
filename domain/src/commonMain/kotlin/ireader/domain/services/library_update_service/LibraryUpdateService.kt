package ireader.domain.services.library_update_service

import ireader.core.log.Log
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.data.repository.LibraryUpdateRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.models.library.*
import ireader.domain.notification.NotificationsIds
import ireader.domain.usecases.local.LocalGetBookUseCases
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.utils.NotificationManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.ExperimentalTime

/**
 * Enhanced library update service following Mihon's comprehensive patterns
 */
@OptIn(ExperimentalTime::class)
class LibraryUpdateService(
    private val getBookUseCases: LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val remoteUseCases: RemoteUseCases,
    private val getLocalCatalog: GetLocalCatalog,
    private val insertUseCases: LocalInsertUseCases,
    private val notificationManager: NotificationManager,
    private val libraryUpdateRepository: LibraryUpdateRepository
) {
    
    private val _updateProgress = MutableStateFlow<LibraryUpdateProgress?>(null)
    val updateProgress: StateFlow<LibraryUpdateProgress?> = _updateProgress.asStateFlow()
    
    private var currentJob: Job? = null
    private var currentJobId: String? = null
    
    /**
     * Execute a library update job
     */
    suspend fun executeUpdate(job: LibraryUpdateJob): LibraryUpdateResult {
        if (currentJob?.isActive == true) {
            throw IllegalStateException("Another update is already running")
        }
        
        currentJobId = job.id
        val startTime = System.currentTimeMillis()
        
        return try {
            currentJob = CoroutineScope(Dispatchers.IO).launch {
                performUpdate(job)
            }
            
            currentJob?.join()
            
            val endTime = System.currentTimeMillis()
            val progress = _updateProgress.value
            
            LibraryUpdateResult(
                jobId = job.id,
                totalBooks = progress?.totalBooks ?: 0,
                updatedBooks = progress?.processedBooks ?: 0,
                newChapters = progress?.newChaptersFound ?: 0,
                skippedBooks = 0,
                errors = progress?.errors ?: emptyList(),
                duration = endTime - startTime
            )
        } catch (e: Exception) {
            Log.error(e) { "Library update failed: ${e.message}" }
            LibraryUpdateResult(
                jobId = job.id,
                totalBooks = 0,
                updatedBooks = 0,
                newChapters = 0,
                skippedBooks = 0,
                errors = listOf(
                    LibraryUpdateError(
                        bookId = 0,
                        bookTitle = "System",
                        sourceId = 0,
                        error = e.message ?: "Unknown error"
                    )
                ),
                duration = System.currentTimeMillis() - startTime
            )
        } finally {
            currentJob = null
            currentJobId = null
            _updateProgress.value = null
            notificationManager.cancel(NotificationsIds.ID_LIBRARY_PROGRESS)
        }
    }
    
    /**
     * Cancel the current update
     */
    suspend fun cancelUpdate(): Boolean {
        return try {
            currentJob?.cancel()
            currentJob = null
            currentJobId = null
            
            _updateProgress.value?.let { progress ->
                _updateProgress.value = progress.copy(isCancelled = true)
            }
            
            notificationManager.cancel(NotificationsIds.ID_LIBRARY_PROGRESS)
            true
        } catch (e: Exception) {
            Log.error(e) { "Failed to cancel update: ${e.message}" }
            false
        }
    }
    
    /**
     * Check if an update can be executed
     */
    suspend fun canExecuteUpdate(): Boolean {
        return currentJob?.isActive != true
    }
    
    /**
     * Get the current update progress
     */
    fun getCurrentProgress(): StateFlow<LibraryUpdateProgress?> {
        return updateProgress
    }
    
    private suspend fun performUpdate(job: LibraryUpdateJob) {
        val libraryBooks = getEligibleBooks(job)
        val errors = mutableListOf<LibraryUpdateError>()
        var newChaptersFound = 0
        var processedBooks = 0
        
        // Initialize progress
        _updateProgress.value = LibraryUpdateProgress(
            jobId = job.id,
            totalBooks = libraryBooks.size,
            processedBooks = 0,
            newChaptersFound = 0,
            errors = emptyList()
        )
        
        // Update notification
        updateNotification(job.id, libraryBooks.size, 0, "Starting update...")
        
        // Process books with concurrency control
        val semaphore = Semaphore(job.maxConcurrentUpdates)
        val jobs = libraryBooks.map { book ->
            async {
                semaphore.withPermit {
                    try {
                        val newChapters = updateBook(book, job)
                        synchronized(this@LibraryUpdateService) {
                            processedBooks++
                            newChaptersFound += newChapters
                            
                            _updateProgress.value = LibraryUpdateProgress(
                                jobId = job.id,
                                totalBooks = libraryBooks.size,
                                processedBooks = processedBooks,
                                currentBookTitle = book.title,
                                newChaptersFound = newChaptersFound,
                                errors = errors.toList()
                            )
                            
                            updateNotification(
                                job.id,
                                libraryBooks.size,
                                processedBooks,
                                book.title
                            )
                        }
                    } catch (e: Exception) {
                        synchronized(this@LibraryUpdateService) {
                            errors.add(
                                LibraryUpdateError(
                                    bookId = book.id,
                                    bookTitle = book.title,
                                    sourceId = book.sourceId,
                                    error = e.message ?: "Unknown error"
                                )
                            )
                            processedBooks++
                        }
                        Log.error(e) { "Failed to update book: ${book.title}" }
                    }
                }
            }
        }
        
        // Wait for all updates to complete
        jobs.awaitAll()
        
        // Mark as completed
        _updateProgress.value = _updateProgress.value?.copy(
            isCompleted = true,
            processedBooks = libraryBooks.size
        )
        
        // Show completion notification
        showCompletionNotification(job.id, libraryBooks.size, newChaptersFound, errors.size)
    }
    
    private suspend fun getEligibleBooks(job: LibraryUpdateJob): List<Book> {
        val allBooks = if (job.onlyFavorites) {
            getBookUseCases.findAllInLibraryBooks().filter { it.favorite }
        } else {
            getBookUseCases.findAllInLibraryBooks()
        }
        
        return allBooks.filter { book ->
            // Filter by categories if specified
            if (job.categoryIds.isNotEmpty()) {
                // Assuming book has category information
                // This would need to be implemented based on your category system
                true // Placeholder
            } else {
                true
            }
        }.filter { book ->
            // Filter by sources if specified
            if (job.sourceIds.isNotEmpty()) {
                book.sourceId in job.sourceIds
            } else {
                true
            }
        }.filter { book ->
            // Apply update strategy
            when (job.updateStrategy) {
                UpdateStrategy.ALWAYS_UPDATE -> true
                UpdateStrategy.FETCH_ONCE -> book.lastUpdate == 0L
                UpdateStrategy.SMART_UPDATE -> shouldUpdateBasedOnPattern(book)
            }
        }.filter { book ->
            // Skip completed books if requested
            if (job.skipCompleted) {
                book.status != Book.COMPLETED
            } else {
                true
            }
        }.filter { book ->
            // Skip read books if requested
            if (job.skipRead) {
                val chapters = runCatching { 
                    getChapterUseCase.findChaptersByBookId(book.id) 
                }.getOrElse { emptyList() }
                chapters.any { !it.read }
            } else {
                true
            }
        }
    }
    
    private suspend fun updateBook(book: Book, job: LibraryUpdateJob): Int {
        val source = getLocalCatalog.get(book.sourceId) ?: return 0
        val existingChapters = getChapterUseCase.findChaptersByBookId(book.id)
        val remoteChapters = mutableListOf<Chapter>()
        
        // Fetch remote chapters
        remoteUseCases.getRemoteChapters(
            book = book,
            source = source,
            onRemoteSuccess = { chapters ->
                remoteChapters.addAll(chapters)
            },
            onError = { error ->
                throw Exception("Failed to fetch chapters: $error")
            },
            oldChapters = existingChapters,
            onSuccess = {}
        )
        
        // Find new chapters
        val existingKeys = existingChapters.map { it.key }.toSet()
        val newChapters = remoteChapters.filter { it.key !in existingKeys }
        
        if (newChapters.isNotEmpty()) {
            // Insert new chapters
            insertUseCases.insertChapters(
                newChapters.map { chapter ->
                    chapter.copy(
                        bookId = book.id,
                        dateFetch = kotlin.time.Clock.System.now().toEpochMilliseconds()
                    )
                }
            )
            
            // Update book's last update time
            insertUseCases.updateBook.update(
                book.copy(
                    lastUpdate = kotlin.time.Clock.System.now().toEpochMilliseconds()
                )
            )
        }
        
        return newChapters.size
    }
    
    private suspend fun shouldUpdateBasedOnPattern(book: Book): Boolean {
        // Smart update logic based on release patterns
        val timeSinceLastUpdate = System.currentTimeMillis() - book.lastUpdate
        val daysSinceUpdate = timeSinceLastUpdate / (24 * 60 * 60 * 1000)
        
        return when (book.status) {
            Book.ONGOING -> daysSinceUpdate >= 1 // Check daily for ongoing
            Book.COMPLETED -> daysSinceUpdate >= 7 // Check weekly for completed
            else -> daysSinceUpdate >= 3 // Check every 3 days for unknown status
        }
    }
    
    private fun updateNotification(
        jobId: String,
        totalBooks: Int,
        processedBooks: Int,
        currentBook: String
    ) {
        // Update progress notification
        // This would integrate with your notification system
        Log.debug { "Update progress: $processedBooks/$totalBooks - $currentBook" }
    }
    
    private fun showCompletionNotification(
        jobId: String,
        totalBooks: Int,
        newChapters: Int,
        errors: Int
    ) {
        // Show completion notification
        Log.info { "Library update completed: $totalBooks books, $newChapters new chapters, $errors errors" }
    }
}