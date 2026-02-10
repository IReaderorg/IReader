package ireader.domain.usecases.source

import ireader.core.log.Log
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.History

/**
 * Use case to insert chapters during migration while preserving existing reading data.
 * 
 * When a chapter with the same name already exists, this use case will:
 * 1. Preserve the existing chapter's read status, lastPageRead, and bookmark
 * 2. Preserve the existing history record (readAt, readDuration, progress)
 * 3. Update only the content-related fields (key, url, dateUpload, etc.)
 * 
 * This ensures that reading progress is never lost during source migration.
 */
class MigrateChaptersWithPreservationUseCase(
    private val chapterRepository: ChapterRepository,
    private val historyRepository: HistoryRepository
) {
    
    /**
     * Result of the migration operation
     */
    data class MigrationResult(
        val totalChapters: Int,
        val preservedChapters: Int,
        val newChapters: Int,
        val preservedHistories: Int
    )
    
    /**
     * Insert new chapters while preserving existing reading data.
     * 
     * @param bookId The book ID
     * @param newChapters List of new chapters from the target source
     * @return MigrationResult with statistics about the operation
     */
    suspend operator fun invoke(
        bookId: Long,
        newChapters: List<Chapter>
    ): MigrationResult {
        Log.info { "Starting chapter migration with preservation for bookId=$bookId, newChapters=${newChapters.size}" }
        
        // Step 1: Get existing chapters and their history
        val existingChapters = chapterRepository.findChaptersByBookId(bookId)
        Log.info { "Found ${existingChapters.size} existing chapters" }
        
        // Create a map by chapter name (case-insensitive for better matching)
        val existingByName = existingChapters.associateBy { it.name.lowercase().trim() }
        
        // Get all histories for existing chapters
        val existingHistories = mutableMapOf<Long, History>()
        existingChapters.forEach { chapter ->
            val history = historyRepository.findHistoryByChapterId(chapter.id)
            if (history != null) {
                existingHistories[chapter.id] = history
            }
        }
        Log.info { "Found ${existingHistories.size} existing history records" }
        
        // Step 2: Prepare chapters to insert with preserved data
        val chaptersToInsert = mutableListOf<Chapter>()
        val historiesToRestore = mutableListOf<History>()
        var preservedCount = 0
        var newCount = 0
        
        newChapters.forEach { newChapter ->
            val existingChapter = existingByName[newChapter.name.lowercase().trim()]
            
            if (existingChapter != null) {
                // Chapter exists - preserve reading data AND content
                val preservedChapter = newChapter.copy(
                    id = existingChapter.id, // Keep the same ID
                    read = existingChapter.read,
                    lastPageRead = existingChapter.lastPageRead,
                    bookmark = existingChapter.bookmark,
                    content = existingChapter.content // Preserve downloaded content!
                )
                chaptersToInsert.add(preservedChapter)
                
                // Also preserve history if it exists
                val existingHistory = existingHistories[existingChapter.id]
                if (existingHistory != null) {
                    historiesToRestore.add(existingHistory)
                }
                preservedCount++
            } else {
                // New chapter - insert as-is
                chaptersToInsert.add(newChapter)
                newCount++
            }
        }
        
        Log.info { "Prepared $preservedCount chapters for preservation, $newCount new chapters" }
        
        // Step 3: Delete old chapters (this cascades to history due to FK)
        chapterRepository.deleteChaptersByBookId(bookId)
        
        // Step 4: Insert chapters with preserved data
        chapterRepository.insertChapters(chaptersToInsert)
        
        // Step 5: Restore history records
        if (historiesToRestore.isNotEmpty()) {
            // Get the newly inserted chapters to map names to new IDs
            val insertedChapters = chapterRepository.findChaptersByBookId(bookId)
            val insertedByName = insertedChapters.associateBy { it.name.lowercase().trim() }
            
            val historiesToInsert = mutableListOf<History>()
            historiesToRestore.forEach { oldHistory ->
                // Find the chapter with the same name to get the new ID
                val oldChapter = existingByName.entries.find { it.value.id == oldHistory.chapterId }
                if (oldChapter != null) {
                    val newChapter = insertedByName[oldChapter.key]
                    if (newChapter != null) {
                        historiesToInsert.add(
                            History(
                                id = 0, // Auto-generate
                                chapterId = newChapter.id,
                                readAt = oldHistory.readAt,
                                readDuration = oldHistory.readDuration,
                                progress = oldHistory.progress
                            )
                        )
                    }
                }
            }
            
            if (historiesToInsert.isNotEmpty()) {
                Log.info { "Restoring ${historiesToInsert.size} history records" }
                historyRepository.insertHistories(historiesToInsert)
            }
        }
        
        val result = MigrationResult(
            totalChapters = newChapters.size,
            preservedChapters = preservedCount,
            newChapters = newCount,
            preservedHistories = historiesToRestore.size
        )
        
        Log.info { "Migration complete: $result" }
        
        return result
    }
}
