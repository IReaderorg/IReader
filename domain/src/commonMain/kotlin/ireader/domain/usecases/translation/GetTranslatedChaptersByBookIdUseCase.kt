package ireader.domain.usecases.translation

import ireader.domain.data.repository.TranslatedChapterRepository
import ireader.domain.models.entities.TranslatedChapter

/**
 * Use case for getting all translated chapters for a book.
 * Used to check if translations are available for EPUB export.
 */
class GetTranslatedChaptersByBookIdUseCase(
    private val repository: TranslatedChapterRepository
) {
    suspend operator fun invoke(bookId: Long): List<TranslatedChapter> {
        return repository.getTranslatedChaptersByBookId(bookId)
    }
    
    /**
     * Check if a book has any translations available.
     */
    suspend fun hasTranslations(bookId: Long): Boolean {
        return repository.getTranslatedChaptersByBookId(bookId).isNotEmpty()
    }
    
    /**
     * Get a map of chapter ID to translated chapter for efficient lookup.
     */
    suspend fun getTranslationsMap(bookId: Long): Map<Long, TranslatedChapter> {
        return repository.getTranslatedChaptersByBookId(bookId)
            .associateBy { it.chapterId }
    }
}
