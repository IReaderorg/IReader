package ireader.data.translation

import ireader.data.chapter.chapterContentConvertor
import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.TranslatedChapterRepository
import ireader.domain.models.entities.TranslatedChapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TranslatedChapterRepositoryImpl(
    private val handler: DatabaseHandler
) : TranslatedChapterRepository {
    
    override suspend fun getTranslatedChapter(
        chapterId: Long,
        targetLanguage: String,
        engineId: Long
    ): TranslatedChapter? {
        // Engine ID is ignored - we just get the most recent translation for this chapter/language
        return handler.awaitOneOrNull {
            translatedChapterQueries.getTranslatedChapterByChapterId(
                chapterId,
                targetLanguage,
                translatedChapterMapper
            )
        }
    }
    
    override suspend fun getTranslatedChapterByLanguage(
        chapterId: Long,
        targetLanguage: String
    ): TranslatedChapter? {
        println("[TranslatedChapterRepositoryImpl] getTranslatedChapterByLanguage: chapterId=$chapterId, targetLanguage=$targetLanguage")
        
        // First, let's check if ANY translations exist for this chapter
        val allForChapter = handler.awaitList {
            translatedChapterQueries.getAllTranslatedChaptersForChapter(chapterId, translatedChapterMapper)
        }
        println("[TranslatedChapterRepositoryImpl] All translations for chapter $chapterId: ${allForChapter.size} found")
        allForChapter.forEach { 
            println("[TranslatedChapterRepositoryImpl]   - id=${it.id}, targetLang=${it.targetLanguage}, engineId=${it.translatorEngineId}, contentSize=${it.translatedContent.size}")
        }
        
        val result = handler.awaitOneOrNull {
            translatedChapterQueries.getTranslatedChapterByChapterIdAndLanguage(
                chapterId,
                targetLanguage,
                translatedChapterMapper
            )
        }
        println("[TranslatedChapterRepositoryImpl] Query result: ${if (result != null) "found" else "not found"}")
        return result
    }
    
    override suspend fun getTranslatedChaptersByBookId(bookId: Long): List<TranslatedChapter> {
        return handler.awaitList {
            translatedChapterQueries.getTranslatedChaptersByBookId(bookId, translatedChapterMapper)
        }
    }
    
    override suspend fun getAllTranslationsForChapter(chapterId: Long): List<TranslatedChapter> {
        return handler.awaitList {
            translatedChapterQueries.getAllTranslatedChaptersForChapter(chapterId, translatedChapterMapper)
        }
    }
    
    override suspend fun insertTranslatedChapter(translatedChapter: TranslatedChapter): Long {
        return handler.awaitOneAsync(inTransaction = true) {
            translatedChapterQueries.insert(
                chapterId = translatedChapter.chapterId,
                bookId = translatedChapter.bookId,
                sourceLanguage = translatedChapter.sourceLanguage,
                targetLanguage = translatedChapter.targetLanguage,
                translatorEngineId = translatedChapter.translatorEngineId,
                translatedContent = translatedChapter.translatedContent,
                createdAt = translatedChapter.createdAt,
                updatedAt = translatedChapter.updatedAt
            )
            translatedChapterQueries.selectLastInsertedRowId()
        }
    }
    
    override suspend fun updateTranslatedChapter(translatedChapter: TranslatedChapter) {
        handler.await(inTransaction = true) {
            translatedChapterQueries.update(
                id = translatedChapter.id,
                translatedContent = translatedChapter.translatedContent,
                updatedAt = translatedChapter.updatedAt
            )
        }
    }
    
    override suspend fun upsertTranslatedChapter(translatedChapter: TranslatedChapter) {
        println("[TranslatedChapterRepositoryImpl] upsertTranslatedChapter: chapterId=${translatedChapter.chapterId}, targetLang=${translatedChapter.targetLanguage}, engineId=${translatedChapter.translatorEngineId}, contentSize=${translatedChapter.translatedContent.size}")
        
        // First, delete any existing translation for this chapter+language combo
        // This ensures INSERT OR REPLACE works correctly
        handler.await(inTransaction = true) {
            // Delete existing translations for this chapter and target language
            translatedChapterQueries.deleteByChapterAndLanguage(
                translatedChapter.chapterId,
                translatedChapter.targetLanguage
            )
            
            // Now insert the new translation
            translatedChapterQueries.upsert(
                chapterId = translatedChapter.chapterId,
                bookId = translatedChapter.bookId,
                sourceLanguage = translatedChapter.sourceLanguage,
                targetLanguage = translatedChapter.targetLanguage,
                translatorEngineId = translatedChapter.translatorEngineId,
                translatedContent = translatedChapter.translatedContent,
                createdAt = translatedChapter.createdAt,
                updatedAt = translatedChapter.updatedAt
            )
        }
        
        // Force WAL checkpoint to ensure data is persisted to main database file
        // This ensures data survives app kill/restart
        handler.checkpoint()
        
        // Verify the save worked
        val verification = handler.awaitList {
            translatedChapterQueries.getAllTranslatedChaptersForChapter(translatedChapter.chapterId, translatedChapterMapper)
        }
        println("[TranslatedChapterRepositoryImpl] After upsert, translations for chapter ${translatedChapter.chapterId}: ${verification.size} found")
        verification.forEach {
            println("[TranslatedChapterRepositoryImpl]   - id=${it.id}, targetLang=${it.targetLanguage}, contentSize=${it.translatedContent.size}")
        }
    }
    
    override suspend fun deleteTranslatedChapter(id: Long) {
        handler.await(inTransaction = true) {
            translatedChapterQueries.deleteTranslatedChapter(id)
        }
    }
    
    override suspend fun deleteTranslatedChaptersByChapterId(chapterId: Long) {
        handler.await(inTransaction = true) {
            translatedChapterQueries.deleteTranslatedChaptersByChapterId(chapterId)
        }
    }
    
    override suspend fun deleteTranslatedChaptersByBookId(bookId: Long) {
        handler.await(inTransaction = true) {
            translatedChapterQueries.deleteTranslatedChaptersByBookId(bookId)
        }
    }
    
    override fun subscribeToTranslatedChaptersByBookId(bookId: Long): Flow<List<TranslatedChapter>> {
        return handler.subscribeToList {
            translatedChapterQueries.getTranslatedChaptersByBookId(bookId, translatedChapterMapper)
        }
    }
}
