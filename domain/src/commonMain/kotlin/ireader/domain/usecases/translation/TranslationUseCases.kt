package ireader.domain.usecases.translation

import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.data.repository.TranslatedChapterRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.TranslatedChapter
import kotlinx.datetime.Clock

class SaveTranslatedChapterUseCase(
    private val repository: TranslatedChapterRepository
) {
    suspend fun execute(
        chapter: Chapter,
        translatedContent: List<Page>,
        sourceLanguage: String,
        targetLanguage: String,
        engineId: Long
    ): Long {
        val now = Clock.System.now().toEpochMilliseconds()
        val translatedChapter = TranslatedChapter(
            chapterId = chapter.id,
            bookId = chapter.bookId,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            translatorEngineId = engineId,
            translatedContent = translatedContent,
            createdAt = now,
            updatedAt = now
        )
        
        repository.upsertTranslatedChapter(translatedChapter)
        return translatedChapter.chapterId
    }
}

class GetTranslatedChapterUseCase(
    private val repository: TranslatedChapterRepository
) {
    suspend fun execute(
        chapterId: Long,
        targetLanguage: String,
        engineId: Long
    ): TranslatedChapter? {
        return repository.getTranslatedChapter(chapterId, targetLanguage, engineId)
    }
}

class DeleteTranslatedChapterUseCase(
    private val repository: TranslatedChapterRepository
) {
    suspend fun execute(chapterId: Long) {
        repository.deleteTranslatedChaptersByChapterId(chapterId)
    }
    
    suspend fun executeForBook(bookId: Long) {
        repository.deleteTranslatedChaptersByBookId(bookId)
    }
}

class GetAllTranslationsForChapterUseCase(
    private val repository: TranslatedChapterRepository
) {
    suspend fun execute(chapterId: Long): List<TranslatedChapter> {
        return repository.getAllTranslationsForChapter(chapterId)
    }
}

class ApplyGlossaryToTextUseCase {
    fun execute(
        text: String,
        glossaryMap: Map<String, String>
    ): String {
        if (glossaryMap.isEmpty()) return text
        
        var result = text
        // Sort by length descending to replace longer terms first
        val sortedEntries = glossaryMap.entries.sortedByDescending { it.key.length }
        
        for ((source, target) in sortedEntries) {
            // Use word boundaries to avoid partial replacements
            result = result.replace(Regex("\\b${Regex.escape(source)}\\b"), target)
        }
        
        return result
    }
    
    fun applyToPages(
        pages: List<Page>,
        glossaryMap: Map<String, String>
    ): List<Page> {
        if (glossaryMap.isEmpty()) return pages
        
        return pages.map { page ->
            when (page) {
                is Text -> Text(execute(page.text, glossaryMap))
                else -> page
            }
        }
    }
}
