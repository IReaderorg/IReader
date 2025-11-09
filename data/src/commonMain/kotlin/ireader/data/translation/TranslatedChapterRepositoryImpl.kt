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
        return handler.awaitOneOrNull {
            translatedChapterQueries.getTranslatedChapterByChapterId(
                chapterId,
                targetLanguage,
                engineId,
                translatedChapterMapper
            )
        }
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
        handler.await(inTransaction = true) {
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
