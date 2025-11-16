package ireader.data.repository

import ireader.core.log.Log
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.MigrationRepository
import ireader.domain.models.migration.MigrationHistory
import ireader.domain.usecases.migration.ChapterMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory implementation of MigrationRepository
 * For production, this should be backed by a database
 */
class MigrationRepositoryImpl(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository
) : MigrationRepository {
    
    // In-memory storage (should be replaced with database in production)
    private val migrationHistoryMap = mutableMapOf<String, MigrationHistory>()
    private val chapterMappingsMap = mutableMapOf<String, List<ChapterMapper.ChapterMapping>>()
    private val _migrationHistoryFlow = MutableStateFlow<List<MigrationHistory>>(emptyList())
    
    override suspend fun saveMigrationHistory(history: MigrationHistory) {
        migrationHistoryMap[history.id] = history
        _migrationHistoryFlow.value = migrationHistoryMap.values.toList()
        Log.info("Saved migration history: ${history.id}")
    }
    
    override suspend fun getMigrationHistory(bookId: Long): MigrationHistory? {
        return migrationHistoryMap.values.find { 
            it.oldBookId == bookId || it.newBookId == bookId 
        }
    }
    
    override fun getAllMigrationHistory(): Flow<List<MigrationHistory>> {
        return _migrationHistoryFlow.asStateFlow()
    }
    
    override suspend fun saveChapterMappings(
        migrationId: String,
        mappings: List<ChapterMapper.ChapterMapping>
    ) {
        chapterMappingsMap[migrationId] = mappings
        Log.info("Saved ${mappings.size} chapter mappings for migration: $migrationId")
    }
    
    override suspend fun getChapterMappings(migrationId: String): List<ChapterMapper.ChapterMapping> {
        return chapterMappingsMap[migrationId] ?: emptyList()
    }
    
    override suspend fun rollbackMigration(migrationId: String): Result<Unit> {
        return try {
            val history = migrationHistoryMap[migrationId]
            if (history == null) {
                return Result.failure(Exception("Migration history not found"))
            }
            
            if (!history.canRollback) {
                return Result.failure(Exception("Migration cannot be rolled back"))
            }
            
            // Get the old and new books
            val oldBook = bookRepository.findBookById(history.oldBookId)
            val newBook = bookRepository.findBookById(history.newBookId)
            
            if (oldBook == null) {
                return Result.failure(Exception("Original book not found"))
            }
            
            // Restore old book to library
            bookRepository.updateBook(oldBook.copy(favorite = true))
            
            // Remove new book from library (or delete it)
            if (newBook != null) {
                bookRepository.updateBook(newBook.copy(favorite = false))
            }
            
            // Get chapter mappings
            val mappings = getChapterMappings(migrationId)
            
            // Restore reading progress to old chapters
            for (mapping in mappings) {
                val newChapter = chapterRepository.findChapterById(mapping.newChapterId)
                if (newChapter != null && newChapter.read) {
                    val oldChapter = chapterRepository.findChapterById(mapping.oldChapterId)
                    if (oldChapter != null) {
                        chapterRepository.insertChapter(
                            oldChapter.copy(
                                read = true,
                                lastPageRead = newChapter.lastPageRead
                            )
                        )
                    }
                }
            }
            
            // Mark migration as rolled back
            migrationHistoryMap[migrationId] = history.copy(canRollback = false)
            _migrationHistoryFlow.value = migrationHistoryMap.values.toList()
            
            Log.info("Successfully rolled back migration: $migrationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.error("Failed to rollback migration: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun deleteMigrationHistory(migrationId: String) {
        migrationHistoryMap.remove(migrationId)
        chapterMappingsMap.remove(migrationId)
        _migrationHistoryFlow.value = migrationHistoryMap.values.toList()
        Log.info("Deleted migration history: $migrationId")
    }
    
    override suspend fun isMigrated(bookId: Long): Boolean {
        return migrationHistoryMap.values.any { 
            it.oldBookId == bookId || it.newBookId == bookId 
        }
    }
}
