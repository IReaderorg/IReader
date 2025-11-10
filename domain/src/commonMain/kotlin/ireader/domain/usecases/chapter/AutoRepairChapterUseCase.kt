package ireader.domain.usecases.chapter

import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.ChapterHealthRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.ChapterHealth
import ireader.domain.services.ChapterHealthChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
     * Attempts to repair a broken chapter by finding a working version from other sources
     * 
     * @param chapter The broken chapter to repair
     * @param book The book containing the chapter
     * @return Result containing the repaired chapter or an error
     */
    suspend operator fun invoke(
        chapter: Chapter,
        book: Book
    ): Result<Chapter> = withContext(Dispatchers.IO) {
        try {
            // Check if repair was recently attempted (within 24 hours)
            val existingHealth = chapterHealthRepository.getChapterHealthById(chapter.id)
            if (existingHealth?.repairAttemptedAt != null) {
                val hoursSinceAttempt = (System.currentTimeMillis() - existingHealth.repairAttemptedAt) / (1000 * 60 * 60)
                if (hoursSinceAttempt < 24) {
                    return@withContext Result.failure(
                        Exception("Repair already attempted within the last 24 hours")
                    )
                }
            }
            
            // Get all installed catalogs
            val catalogs = catalogStore.catalogs
            
            // Search for the novel in other sources
            for (catalog in catalogs) {
                // Skip the current source
                if (catalog.sourceId == book.sourceId) continue
                
                try {
                    val source = catalog.source
                    if (source !is ireader.core.source.CatalogSource) continue
                    
                    // Search for the novel by title using title filter
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
                    } ?: continue
                    
                    // Get chapter list from the alternative source
                    val alternativeChapters = source.getChapterList(matchingNovel, emptyList())
                    
                    // Find matching chapter by number or name
                    val matchingChapter = alternativeChapters.firstOrNull { altChapter ->
                        altChapter.number == chapter.number ||
                        altChapter.name.equals(chapter.name, ignoreCase = true)
                    } ?: continue
                    
                    // Fetch the chapter content
                    val chapterContent = source.getPageList(matchingChapter, emptyList())
                    
                    // Validate the replacement chapter
                    if (!chapterHealthChecker.isChapterBroken(chapterContent)) {
                        // Create repaired chapter
                        val repairedChapter = chapter.copy(
                            content = chapterContent
                        )
                        
                        // Update chapter in database
                        chapterRepository.insertChapter(repairedChapter)
                        
                        // Record successful repair
                        chapterHealthRepository.upsertChapterHealth(
                            ChapterHealth(
                                chapterId = chapter.id,
                                isBroken = false,
                                breakReason = null,
                                checkedAt = System.currentTimeMillis(),
                                repairAttemptedAt = System.currentTimeMillis(),
                                repairSuccessful = true,
                                replacementSourceId = catalog.sourceId
                            )
                        )
                        
                        return@withContext Result.success(repairedChapter)
                    }
                } catch (e: Exception) {
                    // Continue to next source if this one fails
                    continue
                }
            }
            
            // No working replacement found
            chapterHealthRepository.upsertChapterHealth(
                ChapterHealth(
                    chapterId = chapter.id,
                    isBroken = true,
                    breakReason = chapterHealthChecker.getBreakReason(chapter.content),
                    checkedAt = System.currentTimeMillis(),
                    repairAttemptedAt = System.currentTimeMillis(),
                    repairSuccessful = false,
                    replacementSourceId = null
                )
            )
            
            Result.failure(Exception("No working chapter found in alternative sources"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
