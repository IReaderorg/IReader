package ireader.data.translation

import ireader.core.log.Log
import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.TranslatedChapterRepository
import ireader.domain.models.entities.TranslatedChapter
import kotlinx.coroutines.flow.Flow

/**
 * Repository implementation for translated chapters.
 * 
 * Design principles:
 * - Single source of truth for all translation data
 * - Translations are keyed by (chapterId, targetLanguage) - engine is just metadata
 * - All operations go through this repository
 * - No in-memory caching - always read from DB
 */
class TranslatedChapterRepositoryImpl(
    private val handler: DatabaseHandler
) : TranslatedChapterRepository {

    companion object {
        private const val TAG = "TranslatedChapterRepo"
    }

    /**
     * Get translation for a chapter by language.
     * This is the PRIMARY query - ignores engine ID.
     */
    override suspend fun getTranslatedChapterByLanguage(
        chapterId: Long,
        targetLanguage: String
    ): TranslatedChapter? {
        return try {
            handler.awaitOneOrNull {
                translatedChapterQueries.getByChapterAndLanguage(
                    chapterId,
                    targetLanguage,
                    translatedChapterMapper
                )
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Error getting translation: ${e.message}" }
            null
        }
    }

    /**
     * Legacy method - delegates to getTranslatedChapterByLanguage (ignores engineId)
     */
    override suspend fun getTranslatedChapter(
        chapterId: Long,
        targetLanguage: String,
        engineId: Long
    ): TranslatedChapter? {
        return getTranslatedChapterByLanguage(chapterId, targetLanguage)
    }

    /**
     * Get all translations for a chapter (any language)
     */
    override suspend fun getAllTranslationsForChapter(chapterId: Long): List<TranslatedChapter> {
        return try {
            handler.awaitList {
                translatedChapterQueries.getAllForChapter(chapterId, translatedChapterMapper)
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Error getting all translations: ${e.message}" }
            emptyList()
        }
    }

    /**
     * Get all translations for a book
     */
    override suspend fun getTranslatedChaptersByBookId(bookId: Long): List<TranslatedChapter> {
        return try {
            handler.awaitList {
                translatedChapterQueries.getByBookId(bookId, translatedChapterMapper)
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Error getting book translations: ${e.message}" }
            emptyList()
        }
    }

    /**
     * Save or update a translation.
     * Uses upsert - if translation exists for chapter+language, updates it.
     */
    override suspend fun upsertTranslatedChapter(translatedChapter: TranslatedChapter) {
        try {
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
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to save translation: ${e.message}" }
            throw e
        }
    }

    /**
     * Insert a new translation (use upsert instead for most cases)
     */
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

    /**
     * Update an existing translation by ID
     */
    override suspend fun updateTranslatedChapter(translatedChapter: TranslatedChapter) {
        handler.await(inTransaction = true) {
            translatedChapterQueries.update(
                id = translatedChapter.id,
                translatedContent = translatedChapter.translatedContent,
                updatedAt = translatedChapter.updatedAt
            )
        }
    }

    /**
     * Delete a translation by ID
     */
    override suspend fun deleteTranslatedChapter(id: Long) {
        handler.await(inTransaction = true) {
            translatedChapterQueries.deleteTranslatedChapter(id)
        }
    }

    /**
     * Delete all translations for a chapter
     */
    override suspend fun deleteTranslatedChaptersByChapterId(chapterId: Long) {
        handler.await(inTransaction = true) {
            translatedChapterQueries.deleteByChapterId(chapterId)
        }
    }

    /**
     * Delete all translations for a book
     */
    override suspend fun deleteTranslatedChaptersByBookId(bookId: Long) {
        handler.await(inTransaction = true) {
            translatedChapterQueries.deleteByBookId(bookId)
        }
    }

    /**
     * Subscribe to translations for a book (reactive)
     */
    override fun subscribeToTranslatedChaptersByBookId(bookId: Long): Flow<List<TranslatedChapter>> {
        return handler.subscribeToList {
            translatedChapterQueries.getByBookId(bookId, translatedChapterMapper)
        }
    }
}
