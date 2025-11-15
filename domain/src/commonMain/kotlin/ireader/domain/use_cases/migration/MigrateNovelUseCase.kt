package ireader.domain.use_cases.migration

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
    private val getRemoteChapters: GetRemoteChapters
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
                    Log.error("Error searching for matches: ${error.message}")
                },
                onSuccess = { result ->
                    result.mangas.forEach { mangaInfo ->
                        val confidence = calculateConfidence(
                            originalTitle = originalNovel.title,
                            originalAuthor = originalNovel.author,
                            matchTitle = mangaInfo.title,
                            matchAuthor = mangaInfo.author
                        )
                        
                        if (confidence > 0.3f) { // Only include matches with >30% confidence
                            matches.add(
                                MigrationMatch(
                                    novel = BookItem(
                                        id = 0,
                                        sourceId = targetSourceId,
                                        title = mangaInfo.title,
                                        author = mangaInfo.author,
                                        description = mangaInfo.description,
                                        cover = mangaInfo.cover,
                                        customCover = mangaInfo.cover,
                                        key = mangaInfo.key
                                    ),
                                    confidenceScore = confidence,
                                    matchReason = buildMatchReason(
                                        originalTitle = originalNovel.title,
                                        matchTitle = mangaInfo.title,
                                        originalAuthor = originalNovel.author,
                                        matchAuthor = mangaInfo.author,
                                        confidence = confidence
                                    )
                                )
                            )
                        }
                    }
                }
            )
            
            // Sort by confidence and take top 5
            emit(matches.sortedByDescending { it.confidenceScore }.take(5))
        } catch (e: Exception) {
            Log.error("Error in searchMatches: ${e.message}")
            emit(emptyList())
        }
    }
    
    /**
     * Perform the migration
     */
    suspend fun migrate(request: MigrationRequest, selectedMatch: BookItem): Flow<MigrationProgress> = flow {
        try {
            emit(MigrationProgress(request.novelId, MigrationStatus.SEARCHING, 0.1f, null))
            
            // Get original book
            val originalBook = bookRepository.findBookById(request.novelId)
            if (originalBook == null) {
                emit(MigrationProgress(request.novelId, MigrationStatus.FAILED, 0f, "Original book not found"))
                return@flow
            }
            
            emit(MigrationProgress(request.novelId, MigrationStatus.TRANSFERRING, 0.3f, null))
            
            // Get original chapters
            val originalChapters = chapterRepository.findChaptersByBookId(request.novelId)
            
            emit(MigrationProgress(request.novelId, MigrationStatus.TRANSFERRING, 0.5f, null))
            
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
            
            emit(MigrationProgress(request.novelId, MigrationStatus.TRANSFERRING, 0.7f, null))
            
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
                            transferProgress(originalChapters, chapters, newBookId)
                        }
                    }
                )
            }
            
            emit(MigrationProgress(request.novelId, MigrationStatus.TRANSFERRING, 0.9f, null))
            
            // Update original book to mark as migrated (remove from library)
            bookRepository.updateBook(originalBook.copy(favorite = false))
            
            emit(MigrationProgress(request.novelId, MigrationStatus.COMPLETED, 1.0f, null))
            
        } catch (e: Exception) {
            Log.error("Migration failed: ${e.message}")
            emit(MigrationProgress(request.novelId, MigrationStatus.FAILED, 0f, e.message))
        }
    }
    
    /**
     * Transfer reading progress from old chapters to new chapters
     */
    private suspend fun transferProgress(
        oldChapters: List<Chapter>,
        newChapters: List<Chapter>,
        newBookId: Long
    ) {
        try {
            // Find the last read chapter
            val lastReadChapter = oldChapters.filter { it.read }.maxByOrNull { it.number }
            
            if (lastReadChapter != null) {
                // Try to find matching chapter in new source by number
                val matchingNewChapter = newChapters.find { 
                    it.number == lastReadChapter.number 
                }
                
                if (matchingNewChapter != null) {
                    // Mark chapters up to this point as read
                    newChapters.filter { it.number <= matchingNewChapter.number }.forEach { chapter ->
                        chapterRepository.insertChapter(chapter.copy(read = true))
                    }
                }
            }
        } catch (e: Exception) {
            Log.error("Error transferring progress: ${e.message}")
        }
    }
    
    /**
     * Calculate confidence score using Levenshtein distance and author matching
     */
    private fun calculateConfidence(
        originalTitle: String,
        originalAuthor: String,
        matchTitle: String,
        matchAuthor: String
    ): Float {
        val titleSimilarity = 1.0f - (levenshteinDistance(
            originalTitle.lowercase(),
            matchTitle.lowercase()
        ).toFloat() / maxOf(originalTitle.length, matchTitle.length))
        
        val authorMatch = if (originalAuthor.isNotBlank() && matchAuthor.isNotBlank()) {
            if (originalAuthor.lowercase() == matchAuthor.lowercase()) 1.0f else 0.0f
        } else {
            0.5f // Neutral if author info is missing
        }
        
        // Weight: 70% title similarity, 30% author match
        return (titleSimilarity * 0.7f) + (authorMatch * 0.3f)
    }
    
    /**
     * Build a human-readable match reason
     */
    private fun buildMatchReason(
        originalTitle: String,
        matchTitle: String,
        originalAuthor: String,
        matchAuthor: String,
        confidence: Float
    ): String {
        val reasons = mutableListOf<String>()
        
        if (originalTitle.lowercase() == matchTitle.lowercase()) {
            reasons.add("Exact title match")
        } else if (confidence > 0.7f) {
            reasons.add("Very similar title")
        } else if (confidence > 0.5f) {
            reasons.add("Similar title")
        }
        
        if (originalAuthor.isNotBlank() && matchAuthor.isNotBlank()) {
            if (originalAuthor.lowercase() == matchAuthor.lowercase()) {
                reasons.add("Same author")
            }
        }
        
        return if (reasons.isNotEmpty()) {
            reasons.joinToString(", ")
        } else {
            "Partial match"
        }
    }
    
    /**
     * Calculate Levenshtein distance between two strings
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        
        for (i in 0..len1) {
            dp[i][0] = i
        }
        
        for (j in 0..len2) {
            dp[0][j] = j
        }
        
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return dp[len1][len2]
    }
}
