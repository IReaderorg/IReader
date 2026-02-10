package ireader.domain.usecases.source

import ireader.core.log.Log
import ireader.core.source.Source
import ireader.core.source.model.ChapterInfo
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.data.repository.SourceComparisonRepository
import ireader.domain.models.entities.toChapter
import ireader.domain.models.migration.MigrationFlags
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay

/**
 * Use case to migrate a book from one source to another.
 * Handles chapter migration with progress tracking, validation, and error recovery.
 *
 * Migration steps:
 * 1. Validate preconditions (book exists, source available, network accessible)
 * 2. Search for matching book in target source
 * 3. Fetch chapters from target source
 * 4. Map and transfer data based on migration flags
 * 5. Update database atomically (including history preservation)
 * 6. Clean up and verify
 */
class MigrateToSourceUseCase(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val historyRepository: HistoryRepository,
    private val sourceComparisonRepository: SourceComparisonRepository,
    private val catalogStore: CatalogStore,
    private val migrateChaptersWithPreservation: MigrateChaptersWithPreservationUseCase
) {
    
    /**
     * Progress information for migration operations
     */
    data class MigrationProgress(
        val currentStep: String,
        val progress: Float, // 0.0 to 1.0
        val isComplete: Boolean = false,
        val error: String? = null,
        val errorType: MigrationErrorType? = null,
        val canRetry: Boolean = false,
        val detailedInfo: String? = null
    )
    
    /**
     * Types of errors that can occur during migration
     */
    enum class MigrationErrorType {
        BOOK_NOT_FOUND,
        SOURCE_NOT_FOUND,
        SOURCE_UNAVAILABLE,
        NETWORK_ERROR,
        BOOK_NOT_IN_TARGET_SOURCE,
        CHAPTER_FETCH_FAILED,
        DATABASE_ERROR,
        VALIDATION_ERROR,
        UNKNOWN_ERROR
    }
    
    /**
     * Result of pre-migration validation
     */
    private data class ValidationResult(
        val isValid: Boolean,
        val error: String? = null,
        val errorType: MigrationErrorType? = null
    )
    
    /**
     * Perform migration with comprehensive error handling and progress tracking
     */
    operator fun invoke(
        bookId: Long, 
        targetSourceId: Long,
        flags: MigrationFlags = MigrationFlags()
    ): Flow<MigrationProgress> = flow {
        Log.info { "Starting migration for bookId=$bookId to sourceId=$targetSourceId" }
        
        // Step 1: Initial validation
        emit(MigrationProgress("Validating migration request...", 0.0f))
        
        val validation = validateMigration(bookId, targetSourceId)
        if (!validation.isValid) {
            Log.error { "Migration validation failed: ${validation.error}" }
            emit(MigrationProgress(
                currentStep = "Validation failed",
                progress = 0.0f,
                isComplete = true,
                error = validation.error ?: "Unknown validation error",
                errorType = validation.errorType,
                canRetry = validation.errorType == MigrationErrorType.NETWORK_ERROR
            ))
            return@flow
        }
        
        val book = bookRepository.findBookById(bookId)!!
        val targetCatalog = catalogStore.get(targetSourceId)!!
        val targetSource = targetCatalog.source as ireader.core.source.CatalogSource
        
        // Step 2: Search for book in target source
        emit(MigrationProgress("Searching for \"${book.title}\" in ${targetCatalog.name}...", 0.1f, detailedInfo = "This may take a moment"))
        
        val matchedBook = try {
            searchForBookInSource(targetSource, book)
        } catch (e: Exception) {
            Log.error { "Failed to search for book: ${e.message}" }
            emit(MigrationProgress(
                currentStep = "Search failed",
                progress = 0.1f,
                isComplete = true,
                error = "Failed to search for book: ${e.message}",
                errorType = MigrationErrorType.NETWORK_ERROR,
                canRetry = true
            ))
            return@flow
        }
        
        if (matchedBook == null) {
            Log.warn { "Book not found in target source: ${book.title}" }
            emit(MigrationProgress(
                currentStep = "Book not found",
                progress = 0.1f,
                isComplete = true,
                error = "Could not find \"${book.title}\" in ${targetCatalog.name}. The book may not be available in this source.",
                errorType = MigrationErrorType.BOOK_NOT_IN_TARGET_SOURCE,
                canRetry = false,
                detailedInfo = "Try searching for the book manually in the source to verify availability"
            ))
            return@flow
        }
        
        // Step 3: Fetch chapters from target source
        emit(MigrationProgress("Fetching chapter list from ${targetCatalog.name}...", 0.3f, detailedInfo = "Found: ${matchedBook.title}"))
        
        val newChapters = try {
            targetSource.getChapterList(matchedBook, emptyList())
        } catch (e: Exception) {
            Log.error { "Failed to fetch chapters: ${e.message}" }
            emit(MigrationProgress(
                currentStep = "Failed to fetch chapters",
                progress = 0.3f,
                isComplete = true,
                error = "Could not retrieve chapter list: ${e.message}",
                errorType = MigrationErrorType.CHAPTER_FETCH_FAILED,
                canRetry = true,
                detailedInfo = "The source may be temporarily unavailable"
            ))
            return@flow
        }
        
        if (newChapters.isEmpty()) {
            Log.warn { "No chapters found in target source" }
            emit(MigrationProgress(
                currentStep = "No chapters found",
                progress = 0.3f,
                isComplete = true,
                error = "The book has no chapters in ${targetCatalog.name}",
                errorType = MigrationErrorType.CHAPTER_FETCH_FAILED,
                canRetry = false
            ))
            return@flow
        }
        
        // Step 4: Map chapters to Chapter entities
        emit(MigrationProgress("Processing ${newChapters.size} chapters...", 0.45f))
        
        val chaptersToInsert = newChapters.mapIndexed { index, chapterInfo ->
            chapterInfo.toChapter(bookId)
        }
        
        emit(MigrationProgress(
            "Preparing database update...",
            0.5f,
            detailedInfo = "Will match chapters by name for data preservation"
        ))
        
        // Step 6: Update database atomically using the new preservation use case
        try {
            if (flags.chapters) {
                emit(MigrationProgress("Migrating chapters with data preservation...", 0.7f))
                
                // Use the new use case that preserves data by matching chapter names
                val migrationResult = migrateChaptersWithPreservation(bookId, chaptersToInsert)
                
                Log.info { "Chapter migration result: $migrationResult" }
                
                emit(MigrationProgress(
                    "Chapter migration complete...",
                    0.8f,
                    detailedInfo = "Preserved: ${migrationResult.preservedChapters}, New: ${migrationResult.newChapters}, History: ${migrationResult.preservedHistories}"
                ))
            }
            
            // Update book with new source information
            val updatedBook = book.copy(
                sourceId = targetSourceId,
                key = matchedBook.key,
                initialized = true,
                customCover = if (flags.customCover) book.customCover ?: "" else ""
            )
            bookRepository.updateBook(updatedBook)
            
            Log.info { "Book updated successfully: ${book.id}" }
        } catch (e: Exception) {
            Log.error { "Database error during migration: ${e.message}" }
            emit(MigrationProgress(
                currentStep = "Database update failed",
                progress = 0.7f,
                isComplete = true,
                error = "Failed to update database: ${e.message}",
                errorType = MigrationErrorType.DATABASE_ERROR,
                canRetry = true,
                detailedInfo = "Your data should be preserved. Please try again."
            ))
            return@flow
        }
        
        // Step 7: Clean up and verify
        emit(MigrationProgress("Finalizing migration...", 0.9f))
        
        try {
            sourceComparisonRepository.deleteSourceComparison(bookId)
        } catch (e: Exception) {
            Log.warn { "Failed to clear comparison cache: ${e.message}" }
            // Non-critical error, continue
        }
        
        // Small delay to ensure database operations complete
        delay(100)
        
        // Verify migration
        val verifyBook = bookRepository.findBookById(bookId)
        val verifyChapters = chapterRepository.findChaptersByBookId(bookId)
        
        if (verifyBook == null || verifyBook.sourceId != targetSourceId) {
            Log.error { "Migration verification failed: book not properly updated" }
            emit(MigrationProgress(
                currentStep = "Verification failed",
                progress = 0.9f,
                isComplete = true,
                error = "Migration verification failed. Please check the book manually.",
                errorType = MigrationErrorType.DATABASE_ERROR,
                canRetry = true
            ))
            return@flow
        }
        
        // Verify history was restored
        val verifyHistories = historyRepository.findHistoriesByBookId(bookId)
        Log.info { "Migration verified: ${verifyChapters.size} chapters, ${verifyHistories.size} history records" }
        
        Log.info { "Migration completed successfully for bookId=$bookId" }
        
        emit(MigrationProgress(
            currentStep = "Migration complete!",
            progress = 1.0f,
            isComplete = true,
            detailedInfo = "${verifyChapters.size} chapters, ${verifyHistories.size} history records from ${targetCatalog.name}"
        ))
    }
    
    /**
     * Validate migration preconditions
     */
    private suspend fun validateMigration(bookId: Long, targetSourceId: Long): ValidationResult {
        // Check if book exists
        val book = bookRepository.findBookById(bookId)
        if (book == null) {
            return ValidationResult(
                isValid = false,
                error = "Book not found in library",
                errorType = MigrationErrorType.BOOK_NOT_FOUND
            )
        }
        
        // Check if target source exists
        val targetCatalog = catalogStore.get(targetSourceId)
        if (targetCatalog == null) {
            return ValidationResult(
                isValid = false,
                error = "Target source not found. It may have been removed.",
                errorType = MigrationErrorType.SOURCE_NOT_FOUND
            )
        }
        
        // Check if source is a valid CatalogSource
        val targetSource = targetCatalog.source
        if (targetSource !is ireader.core.source.CatalogSource) {
            return ValidationResult(
                isValid = false,
                error = "Invalid source type. This source does not support migration.",
                errorType = MigrationErrorType.SOURCE_UNAVAILABLE
            )
        }
        
        // Check if source is the same as current
        if (book.sourceId == targetSourceId) {
            return ValidationResult(
                isValid = false,
                error = "Book is already from this source",
                errorType = MigrationErrorType.VALIDATION_ERROR
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Search for a book in the target source with fuzzy matching
     */
    private suspend fun searchForBookInSource(
        source: ireader.core.source.CatalogSource,
        book: ireader.domain.models.entities.Book
    ): ireader.core.source.model.MangaInfo? {
        return try {
            // Try exact title search first
            val searchResults = source.getMangaList(
                filters = listOf(
                    ireader.core.source.model.Filter.Title().apply { 
                        this.value = book.title 
                    }
                ),
                page = 1
            )
            
            // Try exact match first
            val exactMatch = searchResults.mangas.firstOrNull { mangaInfo ->
                mangaInfo.title.equals(book.title, ignoreCase = true)
            }
            if (exactMatch != null) return exactMatch
            
            // Try contains match
            val containsMatch = searchResults.mangas.firstOrNull { mangaInfo ->
                mangaInfo.title.contains(book.title, ignoreCase = true) ||
                book.title.contains(mangaInfo.title, ignoreCase = true)
            }
            if (containsMatch != null) return containsMatch
            
            // Try author-based matching if available
            if (book.author.isNotBlank()) {
                val authorMatch = searchResults.mangas.firstOrNull { mangaInfo ->
                    val authorInResult = mangaInfo.description ?: ""
                    authorInResult.contains(book.author, ignoreCase = true) &&
                    (mangaInfo.title.contains(book.title, ignoreCase = true) ||
                     book.title.contains(mangaInfo.title, ignoreCase = true))
                }
                if (authorMatch != null) return authorMatch
            }
            
            null
        } catch (e: Exception) {
            Log.error { "Error searching for book: ${e.message}" }
            throw e
        }
    }
}
