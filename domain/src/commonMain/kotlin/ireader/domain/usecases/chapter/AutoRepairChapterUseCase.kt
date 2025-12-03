package ireader.domain.usecases.chapter
import ireader.domain.utils.extensions.ioDispatcher

import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.ChapterHealthRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.ChapterHealth
import ireader.domain.services.ChapterHealthChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Use case for automatically repairing broken chapters by searching alternative sources
 */
class AutoRepairChapterUseCase(
    private val chapterRepository: ChapterRepository,
    private val chapterHealthRepository: ChapterHealthRepository,
    private val catalogStore: CatalogStore,
    private val chapterHealthChecker: ChapterHealthChecker
) {
    
    /**
     * Attempts to repair a broken chapter by finding a working version.
     * First tries to re-fetch from the book's original source, then searches alternative sources.
     * 
     * @param chapter The broken chapter to repair
     * @param book The book containing the chapter
     * @return Result containing the repaired chapter or an error
     */
    suspend operator fun invoke(
        chapter: Chapter,
        book: Book
    ): Result<Chapter> = withContext(ioDispatcher) {
        try {
            // Check if repair was recently attempted (within 24 hours)
            val existingHealth = chapterHealthRepository.getChapterHealthById(chapter.id)
            if (existingHealth?.repairAttemptedAt != null) {
                val hoursSinceAttempt = (currentTimeToLong() - existingHealth.repairAttemptedAt) / (1000 * 60 * 60)
                if (hoursSinceAttempt < 24) {
                    return@withContext Result.failure(
                        Exception("Repair already attempted within the last 24 hours")
                    )
                }
            }
            
            // Get all installed catalogs
            val catalogs = catalogStore.catalogs
            
            // STEP 1: Try the book's original source first
            val originalCatalog = catalogs.firstOrNull { it.sourceId == book.sourceId }
            if (originalCatalog != null) {
                val result = tryRepairFromSource(
                    catalog = originalCatalog,
                    chapter = chapter,
                    book = book,
                    isOriginalSource = true
                )
                if (result != null) {
                    return@withContext Result.success(result)
                }
            }
            
            // STEP 2: Try other sources if original source failed
            for (catalog in catalogs) {
                // Skip the original source (already tried)
                if (catalog.sourceId == book.sourceId) continue
                
                val result = tryRepairFromSource(
                    catalog = catalog,
                    chapter = chapter,
                    book = book,
                    isOriginalSource = false
                )
                if (result != null) {
                    return@withContext Result.success(result)
                }
            }
            
            // No working replacement found
            chapterHealthRepository.upsertChapterHealth(
                ChapterHealth(
                    chapterId = chapter.id,
                    isBroken = true,
                    breakReason = chapterHealthChecker.getBreakReason(chapter.content),
                    checkedAt = currentTimeToLong(),
                    repairAttemptedAt = currentTimeToLong(),
                    repairSuccessful = false,
                    replacementSourceId = null
                )
            )
            
            Result.failure(Exception("No working chapter found from any source"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Attempts to repair a chapter from a specific source
     * 
     * @param catalog The catalog to try
     * @param chapter The broken chapter
     * @param book The book containing the chapter
     * @param isOriginalSource Whether this is the book's original source
     * @return The repaired chapter if successful, null otherwise
     */
    private suspend fun tryRepairFromSource(
        catalog: ireader.domain.models.entities.CatalogLocal,
        chapter: Chapter,
        book: Book,
        isOriginalSource: Boolean
    ): Chapter? {
        try {
            val source = catalog.source
            if (source !is ireader.core.source.CatalogSource) return null
            
            if (isOriginalSource) {
                // For original source, directly re-fetch the chapter content using the existing key
                val chapterInfo = ireader.core.source.model.ChapterInfo(
                    key = chapter.key,
                    name = chapter.name,
                    number = chapter.number,
                    dateUpload = chapter.dateUpload
                )
                
                val chapterContent = source.getPageList(chapterInfo, emptyList())
                
                // Validate the fetched content
                if (chapterContent.isNotEmpty() && !chapterHealthChecker.isChapterBroken(chapterContent)) {
                    val repairedChapter = chapter.copy(content = chapterContent)
                    
                    // Update chapter in database
                    chapterRepository.insertChapter(repairedChapter)
                    
                    // Record successful repair
                    chapterHealthRepository.upsertChapterHealth(
                        ChapterHealth(
                            chapterId = chapter.id,
                            isBroken = false,
                            breakReason = null,
                            checkedAt = currentTimeToLong(),
                            repairAttemptedAt = currentTimeToLong(),
                            repairSuccessful = true,
                            replacementSourceId = catalog.sourceId
                        )
                    )
                    
                    return repairedChapter
                }
            } else {
                // For alternative sources, search for the novel first
                val searchResults = source.getMangaList(
                    filters = listOf(
                        ireader.core.source.model.Filter.Title().apply { 
                            this.value = book.title 
                        }
                    ),
                    page = 1
                )
                
                // Find exact or close match
                val matchingNovel = searchResults.mangas.firstOrNull { mangaInfo ->
                    mangaInfo.title.equals(book.title, ignoreCase = true) ||
                    mangaInfo.title.contains(book.title, ignoreCase = true)
                } ?: return null
                
                // Get chapter list from the alternative source
                val alternativeChapters = source.getChapterList(matchingNovel, emptyList())
                
                // Find matching chapter by name first (most reliable), then by number
                val matchingChapter = findMatchingChapter(chapter, alternativeChapters) ?: return null
                
                // Fetch the chapter content
                val chapterContent = source.getPageList(matchingChapter, emptyList())
                
                // Validate the replacement chapter
                if (chapterContent.isNotEmpty() && !chapterHealthChecker.isChapterBroken(chapterContent)) {
                    val repairedChapter = chapter.copy(content = chapterContent)
                    
                    // Update chapter in database
                    chapterRepository.insertChapter(repairedChapter)
                    
                    // Record successful repair
                    chapterHealthRepository.upsertChapterHealth(
                        ChapterHealth(
                            chapterId = chapter.id,
                            isBroken = false,
                            breakReason = null,
                            checkedAt = currentTimeToLong(),
                            repairAttemptedAt = currentTimeToLong(),
                            repairSuccessful = true,
                            replacementSourceId = catalog.sourceId
                        )
                    )
                    
                    return repairedChapter
                }
            }
        } catch (e: Exception) {
            // Log and continue to next source
            ireader.core.log.Log.debug("Failed to repair from source ${catalog.sourceId}: ${e.message}")
        }
        
        return null
    }
    
    /**
     * Finds a matching chapter from a list of alternative chapters
     */
    private fun findMatchingChapter(
        chapter: Chapter,
        alternativeChapters: List<ireader.core.source.model.ChapterInfo>
    ): ireader.core.source.model.ChapterInfo? {
        // Try exact name match first (case-insensitive)
        alternativeChapters.firstOrNull { altChapter ->
            altChapter.name.equals(chapter.name, ignoreCase = true)
        }?.let { return it }
        
        // Try number match (only if number is valid)
        if (chapter.isRecognizedNumber) {
            alternativeChapters.firstOrNull { altChapter ->
                altChapter.number == chapter.number
            }?.let { return it }
        }
        
        // Fallback: fuzzy name matching (contains key parts)
        val chapterNameNormalized = chapter.name.lowercase().trim()
        if (chapterNameNormalized.isNotEmpty()) {
            alternativeChapters.firstOrNull { altChapter ->
                val altChapterNameNormalized = altChapter.name.lowercase().trim()
                altChapterNameNormalized.contains(chapterNameNormalized) ||
                chapterNameNormalized.contains(altChapterNameNormalized)
            }?.let { return it }
        }
        
        return null
    }
}
