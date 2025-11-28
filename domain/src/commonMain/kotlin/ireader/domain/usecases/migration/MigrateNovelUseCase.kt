package ireader.domain.usecases.migration

import ireader.core.log.Log
import ireader.core.source.model.Filter
import ireader.core.source.model.FilterList
import ireader.core.source.model.Listing
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.toBook
import ireader.domain.models.entities.toBookItem
import ireader.domain.models.migration.MigrationMatch
import ireader.domain.models.migration.MigrationProgress
import ireader.domain.models.migration.MigrationRequest
import ireader.domain.models.migration.MigrationResult
import ireader.domain.models.migration.MigrationStatus
import ireader.domain.usecases.migration.BookMatcher
import ireader.domain.usecases.migration.ChapterMapper
import ireader.domain.usecases.remote.GetRemoteBooksUseCase
import ireader.domain.usecases.remote.GetRemoteChapters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use case for migrating a novel from one source to another
 */
class MigrateNovelUseCase(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val catalogStore: CatalogStore,
    private val getRemoteBooksUseCase: GetRemoteBooksUseCase,
    private val getRemoteChapters: GetRemoteChapters,
    private val bookMatcher: BookMatcher,
    private val chapterMapper: ChapterMapper
) {
    
    /**
     * Search for potential matches in the target source
     */
    suspend fun searchMatches(
        originalNovel: Book,
        targetSourceId: Long
    ): Flow<List<MigrationMatch>> = flow {
        try {
            val targetCatalog = catalogStore.get(targetSourceId)
            if (targetCatalog == null) {
                emit(emptyList())
                return@flow
            }
            
            val matches = mutableListOf<MigrationMatch>()
            
            // Search by title
            getRemoteBooksUseCase(
                query = originalNovel.title,
                listing = null,
                filters = null,
                catalog = targetCatalog,
                page = 1,
                onError = { error ->
                    Log.error { "Error searching for matches: ${error.message}" }
                },
                onSuccess = { result ->
                    val candidates = result.mangas.map { mangaInfo ->
                        BookItem(
                            id = 0,
                            sourceId = targetSourceId,
                            title = mangaInfo.title,
                            author = mangaInfo.author,
                            description = mangaInfo.description,
                            cover = mangaInfo.cover,
                            customCover = mangaInfo.cover,
                            key = mangaInfo.key
                        )
                    }
                    
                    // Use BookMatcher to find and rank matches
                    matches.addAll(bookMatcher.findMatches(originalNovel, candidates))
                }
            )
            
            // Take top 5 matches
            emit(matches.take(5))
        } catch (e: Exception) {
            Log.error { "Error in searchMatches: ${e.message}" }
            emit(emptyList())
        }
    }
    
    /**
     * Perform the migration
     */
    suspend fun migrate(request: MigrationRequest, selectedMatch: BookItem): Flow<MigrationProgress> = flow {
        try {
            emit(MigrationProgress(request.novelId, MigrationStatus.SEARCHING, 0.1f, ""))
            
            // Get original book
            val originalBook = bookRepository.findBookById(request.novelId)
            if (originalBook == null) {
                emit(MigrationProgress(request.novelId, MigrationStatus.FAILED, 0f, "Original book not found"))
                return@flow
            }
            
            emit(MigrationProgress(request.novelId, MigrationStatus.TRANSFERRING_CHAPTERS, 0.3f, ""))
            
            // Get original chapters
            val originalChapters = chapterRepository.findChaptersByBookId(request.novelId)
            
            emit(MigrationProgress(request.novelId, MigrationStatus.TRANSFERRING_CHAPTERS, 0.5f, ""))
            
            // Create new book with target source
            val newBook = originalBook.copy(
                id = 0, // Will be assigned by database
                sourceId = request.targetSourceId,
                key = selectedMatch.key,
                title = selectedMatch.title,
                author = selectedMatch.author,
                description = selectedMatch.description,
                cover = selectedMatch.cover,
                customCover = selectedMatch.customCover,
                initialized = false // Will need to fetch chapters
            )
            
            // Insert new book
            val newBookId = bookRepository.upsert(newBook)
            
            emit(MigrationProgress(request.novelId, MigrationStatus.TRANSFERRING_CHAPTERS, 0.7f, ""))
            
            // Fetch chapters from new source
            val targetCatalog = catalogStore.get(request.targetSourceId)
            if (targetCatalog != null) {
                getRemoteChapters(
                    book = newBook.copy(id = newBookId),
                    catalog = targetCatalog,
                    oldChapters = emptyList(),
                    onError = { error ->
                        Log.error("Error fetching chapters: ${error?.toString()}")
                    },
                    onSuccess = { chapters ->
                            // Transfer reading progress if requested
                        if (request.preserveProgress && originalChapters.isNotEmpty()) {
                            transferProgress(originalChapters, chapters.toList(), newBookId)
                        }
                    }
                )
            }
            
            emit(MigrationProgress(request.novelId, MigrationStatus.TRANSFERRING_CHAPTERS, 0.9f, ""))
            
            // Update original book to mark as migrated (remove from library)
            bookRepository.updateBook(originalBook.copy(favorite = false))
            
            emit(MigrationProgress(request.novelId, MigrationStatus.COMPLETED, 1.0f, ""))
            
        } catch (e: Exception) {
            Log.error("Migration failed: ${e.message}")
            emit(MigrationProgress(request.novelId, MigrationStatus.FAILED, 0f, e.message ?: "Unknown error"))
        }
    }
    
    /**
     * Transfer reading progress from old chapters to new chapters using ChapterMapper
     */
    private suspend fun transferProgress(
        oldChapters: List<Chapter>,
        newChapters: List<Chapter>,
        newBookId: Long
    ) {
        try {
            // Use ChapterMapper to map chapters
            val mappings = chapterMapper.mapChapters(oldChapters, newChapters)
            
            Log.info { "Mapped ${mappings.size} chapters out of ${oldChapters.size}" }
            
            // Transfer reading progress based on mappings
            for (mapping in mappings) {
                val oldChapter = oldChapters.find { it.id == mapping.oldChapterId }
                val newChapter = newChapters.find { it.id == mapping.newChapterId }
                
                if (oldChapter != null && newChapter != null && oldChapter.read) {
                    chapterRepository.insertChapter(
                        newChapter.copy(
                            read = true,
                            lastPageRead = oldChapter.lastPageRead,
                            bookmark = oldChapter.bookmark
                        )
                    )
                }
            }
            
            // Log mapping statistics
            val stats = chapterMapper.getMappingStatistics(mappings)
            Log.info { "Chapter mapping stats: ${stats.exactMatches} exact, ${stats.numberMatches} by number, ${stats.fuzzyMatches} fuzzy" }
        } catch (e: Exception) {
            Log.error { "Error transferring progress: ${e.message}" }
        }
    }
    
}
