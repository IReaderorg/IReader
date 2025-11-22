package ireader.domain.usecases.source

import ireader.core.log.Log
import ireader.core.source.Source
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.SourceComparisonRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.SourceComparison

/**
 * Use case to check if better sources are available for a book
 * Compares chapter counts across all installed sources
 */
class CheckSourceAvailabilityUseCase(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val sourceComparisonRepository: SourceComparisonRepository,
    private val catalogStore: CatalogStore
) {
    
    companion object {
        private const val CACHE_TTL_HOURS = 24
        private const val MIN_CHAPTER_DIFFERENCE = 5
    }
    
    suspend operator fun invoke(bookId: Long): Result<SourceComparison?> {
        return try {
            val book = bookRepository.findBookById(bookId)
                ?: return Result.failure(Exception("Book not found"))
            
            // Check cache first
            val cached = sourceComparisonRepository.getSourceComparisonByBookId(bookId)
            if (cached != null && !isCacheExpired(cached.cachedAt)) {
                // Check if dismissal period is still active
                if (cached.dismissedUntil != null && System.currentTimeMillis() < cached.dismissedUntil) {
                    return Result.success(null) // Don't show banner during dismissal period
                }
                return Result.success(cached)
            }
            
            // Get current chapter count
            val currentChapterCount = chapterRepository.findChaptersByBookId(bookId).size
            
            // Search only pinned sources for the same book (optimization)
            val pinnedCatalogs = catalogStore.catalogs.filter { it.isPinned }
            var betterSourceId: Long? = null
            var maxChapterDifference = 0
            
            Log.info { "Checking ${pinnedCatalogs.size} pinned sources for better alternatives" }
            
            for (catalog in pinnedCatalogs) {
                if (catalog.sourceId == book.sourceId) continue // Skip current source
                
                try {
                    val source = catalog.source
                    if (source !is ireader.core.source.CatalogSource) continue
                    
                    // Search for the book in this source using title filter
                    val searchResults = source.getMangaList(
                        filters = listOf(
                            ireader.core.source.model.Filter.Title().apply { 
                                this.value = book.title 
                            }
                        ),
                        page = 1
                    )
                    
                    // Find exact or close match
                    val matchedBook = searchResults.mangas.firstOrNull { mangaInfo ->
                        mangaInfo.title.equals(book.title, ignoreCase = true) ||
                        mangaInfo.title.contains(book.title, ignoreCase = true) ||
                        book.title.contains(mangaInfo.title, ignoreCase = true)
                    }
                    
                    if (matchedBook != null) {
                        // Get chapter count from this source
                        val chapters = source.getChapterList(matchedBook, emptyList())
                        val chapterDifference = chapters.size - currentChapterCount
                        
                        Log.info { "Found ${chapters.size} chapters in ${catalog.name} vs $currentChapterCount in current source" }
                        
                        if (chapterDifference >= MIN_CHAPTER_DIFFERENCE && chapterDifference > maxChapterDifference) {
                            betterSourceId = catalog.sourceId
                            maxChapterDifference = chapterDifference
                        }
                    }
                } catch (e: Exception) {
                    Log.error { "Error checking source ${catalog.name}: ${e.message}" }
                    // Continue checking other sources
                }
            }
            
            // Create and cache the comparison result
            val comparison = SourceComparison(
                bookId = bookId,
                currentSourceId = book.sourceId,
                betterSourceId = betterSourceId,
                chapterDifference = maxChapterDifference,
                cachedAt = System.currentTimeMillis(),
                dismissedUntil = null
            )
            
            sourceComparisonRepository.upsertSourceComparison(comparison)
            
            Result.success(if (betterSourceId != null) comparison else null)
        } catch (e: Exception) {
            Log.error { "Error checking source availability: ${e.message}" }
            Result.failure(e)
        }
    }
    
    private fun isCacheExpired(cachedAt: Long): Boolean {
        val hoursSinceCached = (System.currentTimeMillis() - cachedAt) / (1000 * 60 * 60)
        return hoursSinceCached >= CACHE_TTL_HOURS
    }
}
