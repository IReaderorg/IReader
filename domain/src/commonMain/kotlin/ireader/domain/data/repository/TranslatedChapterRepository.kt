package ireader.domain.data.repository

import ireader.domain.models.entities.TranslatedChapter
import kotlinx.coroutines.flow.Flow

interface TranslatedChapterRepository {
    suspend fun getTranslatedChapter(
        chapterId: Long,
        targetLanguage: String,
        engineId: Long
    ): TranslatedChapter?
    
    suspend fun getTranslatedChaptersByBookId(bookId: Long): List<TranslatedChapter>
    
    suspend fun getAllTranslationsForChapter(chapterId: Long): List<TranslatedChapter>
    
    suspend fun insertTranslatedChapter(translatedChapter: TranslatedChapter): Long
    
    suspend fun updateTranslatedChapter(translatedChapter: TranslatedChapter)
    
    suspend fun upsertTranslatedChapter(translatedChapter: TranslatedChapter)
    
    suspend fun deleteTranslatedChapter(id: Long)
    
    suspend fun deleteTranslatedChaptersByChapterId(chapterId: Long)
    
    suspend fun deleteTranslatedChaptersByBookId(bookId: Long)
    
    fun subscribeToTranslatedChaptersByBookId(bookId: Long): Flow<List<TranslatedChapter>>
}
