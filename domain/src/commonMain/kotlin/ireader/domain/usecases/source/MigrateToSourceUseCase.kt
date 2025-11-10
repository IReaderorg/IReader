package ireader.domain.usecases.source

import ireader.core.log.Log
import ireader.core.source.Source
import ireader.core.source.model.ChapterInfo
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.SourceComparisonRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.toChapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use case to migrate a book from one source to another
 * Handles chapter migration with progress tracking
 */
class MigrateToSourceUseCase(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val sourceComparisonRepository: SourceComparisonRepository,
    private val catalogStore: CatalogStore
) {
    
    data class MigrationProgress(
        val currentStep: String,
        val progress: Float, // 0.0 to 1.0
        val isComplete: Boolean = false,
        val error: String? = null
    )
    
    operator fun invoke(bookId: Long, targetSourceId: Long): Flow<MigrationProgress> = flow {
        try {
            emit(MigrationProgress("Starting migration...", 0.0f))
            
            // Get the book
            val book = bookRepository.findBookById(bookId)
                ?: throw Exception("Book not found")
            
            emit(MigrationProgress("Finding book in new source...", 0.1f))
            
            // Get the target source
            val targetCatalog = catalogStore.get(targetSourceId)
                ?: throw Exception("Target source not found")
            
            val targetSource = targetCatalog.source
            if (targetSource !is ireader.core.source.CatalogSource) {
                throw Exception("Invalid source type")
            }
            
            // Search for the book in the target source using title filter
            val searchResults = targetSource.getMangaList(
                filters = listOf(
                    ireader.core.source.model.Filter.Title().apply { 
                        this.value = book.title 
                    }
                ),
                page = 1
            )
            
            val matchedBook = searchResults.mangas.firstOrNull { mangaInfo ->
                mangaInfo.title.equals(book.title, ignoreCase = true) ||
                mangaInfo.title.contains(book.title, ignoreCase = true) ||
                book.title.contains(mangaInfo.title, ignoreCase = true)
            } ?: throw Exception("Book not found in target source")
            
            emit(MigrationProgress("Fetching chapters from new source...", 0.3f))
            
            // Get chapters from the new source
            val newChapters = targetSource.getChapterList(matchedBook, emptyList())
            
            emit(MigrationProgress("Mapping chapters...", 0.5f))
            
            // Get existing chapters to preserve read status
            val existingChapters = chapterRepository.findChaptersByBookId(bookId)
            val existingChapterMap = existingChapters.associateBy { it.number }
            
            // Convert new chapters and preserve read status
            val chaptersToInsert = newChapters.mapIndexed { index, chapterInfo ->
                val chapter = chapterInfo.toChapter(bookId)
                val existingChapter = existingChapterMap[chapter.number]
                
                chapter.copy(
                    read = existingChapter?.read ?: false,
                    bookmark = existingChapter?.bookmark ?: false,
                    lastPageRead = existingChapter?.lastPageRead ?: 0
                )
            }
            
            emit(MigrationProgress("Updating database...", 0.7f))
            
            // Delete old chapters
            chapterRepository.deleteChaptersByBookId(bookId)
            
            // Insert new chapters
            chapterRepository.insertChapters(chaptersToInsert)
            
            // Update book with new source and key
            val updatedBook = book.copy(
                sourceId = targetSourceId,
                key = matchedBook.key,
                initialized = true
            )
            bookRepository.updateBook(updatedBook)
            
            emit(MigrationProgress("Cleaning up...", 0.9f))
            
            // Clear the source comparison cache for this book
            sourceComparisonRepository.deleteSourceComparison(bookId)
            
            emit(MigrationProgress("Migration complete!", 1.0f, isComplete = true))
            
        } catch (e: Exception) {
            Log.error { "Error during migration: ${e.message}" }
            emit(MigrationProgress(
                currentStep = "Migration failed",
                progress = 0.0f,
                isComplete = true,
                error = e.message ?: "Unknown error"
            ))
        }
    }
}
