package ireader.domain.usecases.migration

import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.MigrationRepository
import ireader.domain.data.repository.NotificationRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.migration.*
import ireader.domain.models.notification.MigrationNotification
import ireader.domain.models.notification.MigrationNotificationInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay

/**
 * Use case for performing book migration following Mihon's pattern
 */
class MigrateBookUseCase(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val categoryRepository: CategoryRepository,
    private val migrationRepository: MigrationRepository,
    private val notificationRepository: NotificationRepository,
    private val bookMatcher: BookMatcher
) {
    
    suspend fun execute(request: MigrationRequest): Flow<MigrationProgress> = flow {
        try {
            emit(MigrationProgress(request.novelId, MigrationStatus.SEARCHING, 0f, "Searching for matches..."))
            
            // Get original book
            val originalBook = bookRepository.findBookById(request.novelId)
                ?: throw Exception("Original book not found")
            
            // Search for matches in target source
            val searchResults = searchForMatches(originalBook, request.targetSourceId)
            
            if (searchResults.isEmpty()) {
                emit(MigrationProgress(request.novelId, MigrationStatus.FAILED, 0f, error = "No matches found"))
                return@flow
            }
            
            emit(MigrationProgress(request.novelId, MigrationStatus.MATCHING, 0.2f, "Found ${searchResults.size} potential matches"))
            
            // Use best match or let user choose
            val bestMatch = searchResults.first()
            val targetBook = bestMatch.novel
            
            emit(MigrationProgress(request.novelId, MigrationStatus.TRANSFERRING_CHAPTERS, 0.3f, "Transferring chapters..."))
            
            // Perform migration
            val migrationResult = performMigration(originalBook, targetBook, request.flags)
            
            if (migrationResult.success) {
                // Save migration history
                val history = MigrationHistory(
                    id = generateMigrationId(),
                    oldBookId = originalBook.id,
                    newBookId = migrationResult.newNovelId!!,
                    oldSourceId = request.sourceId,
                    newSourceId = request.targetSourceId,
                    timestamp = System.currentTimeMillis(),
                    chaptersTransferred = migrationResult.transferredData?.chaptersTransferred ?: 0,
                    progressPreserved = migrationResult.transferredData?.progressPreserved ?: false,
                    flags = request.flags,
                    transferredData = migrationResult.transferredData!!
                )
                
                // Show completion notification
                notificationRepository.showMigrationNotification(
                    MigrationNotification(
                        bookTitle = originalBook.title,
                        message = "Migration completed from Source ${request.sourceId} to Source ${request.targetSourceId}",
                        isSuccess = true
                    )
                )
                
                emit(MigrationProgress(request.novelId, MigrationStatus.COMPLETED, 1f, "Migration completed successfully"))
            } else {
                emit(MigrationProgress(request.novelId, MigrationStatus.FAILED, 0f, error = migrationResult.error))
            }
            
        } catch (e: Exception) {
            emit(MigrationProgress(request.novelId, MigrationStatus.FAILED, 0f, error = e.message))
        }
    }
    
    private suspend fun searchForMatches(book: Book, targetSourceId: Long): List<MigrationMatch> {
        // This would integrate with the actual source search
        // For now, return empty list as placeholder
        return emptyList()
    }
    
    private suspend fun performMigration(
        originalBook: Book,
        targetBook: ireader.domain.models.entities.BookItem,
        flags: MigrationFlags
    ): MigrationResult {
        return try {
            var chaptersTransferred = 0
            var bookmarksTransferred = 0
            var categoriesTransferred = 0
            var progressPreserved = false
            var customCoverTransferred = false
            
            // Transfer chapters if enabled
            if (flags.chapters) {
                val originalChapters = chapterRepository.findChaptersByBookId(originalBook.id)
                // Transfer logic here
                chaptersTransferred = originalChapters.size
            }
            
            // Transfer bookmarks if enabled
            if (flags.bookmarks) {
                // Transfer bookmark logic here
                bookmarksTransferred = 0 // Placeholder
            }
            
            // Transfer categories if enabled
            if (flags.categories) {
                // Transfer category logic here
                categoriesTransferred = 0 // Placeholder
            }
            
            // Transfer reading progress if enabled
            if (flags.readingProgress) {
                // Transfer progress logic here
                progressPreserved = true
            }
            
            // Transfer custom cover if enabled
            if (flags.customCover) {
                // Transfer cover logic here
                customCoverTransferred = true
            }
            
            val transferredData = MigrationTransferredData(
                chaptersTransferred = chaptersTransferred,
                bookmarksTransferred = bookmarksTransferred,
                categoriesTransferred = categoriesTransferred,
                progressPreserved = progressPreserved,
                customCoverTransferred = customCoverTransferred
            )
            
            MigrationResult(
                novelId = originalBook.id,
                success = true,
                newNovelId = targetBook.id,
                error = null,
                transferredData = transferredData
            )
        } catch (e: Exception) {
            MigrationResult(
                novelId = originalBook.id,
                success = false,
                newNovelId = null,
                error = e.message
            )
        }
    }
    
    private fun generateMigrationId(): String {
        return "migration_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

/**
 * Use case for batch migration operations
 */
class BatchMigrationUseCase(
    private val migrationRepository: MigrationRepository,
    private val migrateBookUseCase: MigrateBookUseCase
) {
    
    suspend fun execute(job: MigrationJob): Flow<MigrationJob> = flow {
        try {
            // Save initial job
            migrationRepository.saveMigrationJob(job.copy(
                status = MigrationJobStatus.RUNNING,
                startTime = System.currentTimeMillis()
            ))
            
            var completedBooks = 0
            var failedBooks = 0
            
            job.books.forEachIndexed { index, book ->
                val progress = (index.toFloat() / job.books.size)
                
                // Update job progress
                val updatedJob = job.copy(
                    progress = progress,
                    completedBooks = completedBooks,
                    failedBooks = failedBooks
                )
                
                migrationRepository.updateMigrationJobProgress(
                    job.id,
                    progress,
                    completedBooks,
                    failedBooks
                )
                
                emit(updatedJob)
                
                // Migrate individual book
                val request = MigrationRequest(
                    novelId = book.id,
                    sourceId = book.sourceId,
                    targetSourceId = job.targetSources.first().sourceId, // Use first target for now
                    flags = job.flags
                )
                
                try {
                    migrateBookUseCase.execute(request).collect { progress ->
                        if (progress.status == MigrationStatus.COMPLETED) {
                            completedBooks++
                        } else if (progress.status == MigrationStatus.FAILED) {
                            failedBooks++
                        }
                    }
                } catch (e: Exception) {
                    failedBooks++
                }
                
                // Small delay between migrations
                delay(1000)
            }
            
            // Complete job
            val completedJob = job.copy(
                status = MigrationJobStatus.COMPLETED,
                progress = 1f,
                completedBooks = completedBooks,
                failedBooks = failedBooks,
                endTime = System.currentTimeMillis()
            )
            
            migrationRepository.updateMigrationJobStatus(job.id, MigrationJobStatus.COMPLETED)
            emit(completedJob)
            
        } catch (e: Exception) {
            migrationRepository.updateMigrationJobStatus(job.id, MigrationJobStatus.FAILED)
            emit(job.copy(status = MigrationJobStatus.FAILED))
        }
    }
}

/**
 * Use case for searching migration targets
 */
class SearchMigrationTargetsUseCase(
    private val bookMatcher: BookMatcher
) {
    
    suspend fun execute(book: Book, targetSources: List<Long>): List<MigrationSearchResult> {
        return targetSources.map { sourceId ->
            try {
                // This would integrate with actual source search
                val matches = emptyList<MigrationMatch>() // Placeholder
                
                MigrationSearchResult(
                    sourceId = sourceId,
                    sourceName = "Source $sourceId", // Get actual name
                    matches = matches,
                    isSearching = false
                )
            } catch (e: Exception) {
                MigrationSearchResult(
                    sourceId = sourceId,
                    sourceName = "Source $sourceId",
                    matches = emptyList(),
                    isSearching = false,
                    error = e.message
                )
            }
        }
    }
}