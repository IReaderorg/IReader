package ireader.domain.usecases.translation

import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.data.repository.TranslatedChapterRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.TranslatedChapter
import ireader.domain.usecases.glossary.DeleteGlossaryEntryUseCase
import ireader.domain.usecases.glossary.ExportGlossaryUseCase
import ireader.domain.usecases.glossary.GetGlossaryByBookIdUseCase
import ireader.domain.usecases.glossary.ImportGlossaryUseCase
import ireader.domain.usecases.glossary.SaveGlossaryEntryUseCase
import ireader.domain.usecases.translate.TranslateChapterWithStorageUseCase
import ireader.domain.usecases.translate.TranslateParagraphUseCase
import kotlin.time.ExperimentalTime

/**
 * Aggregate for translation-related use cases.
 * Groups glossary and chapter translation operations.
 */
data class TranslationUseCases(
    val translateChapter: TranslateChapterWithStorageUseCase,
    val translateParagraph: TranslateParagraphUseCase,
    val getTranslated: GetTranslatedChapterUseCase,
    val getGlossary: GetGlossaryByBookIdUseCase,
    val saveGlossary: SaveGlossaryEntryUseCase,
    val deleteGlossary: DeleteGlossaryEntryUseCase,
    val exportGlossary: ExportGlossaryUseCase,
    val importGlossary: ImportGlossaryUseCase
)

class SaveTranslatedChapterUseCase(
    private val repository: TranslatedChapterRepository
) {
    @OptIn(ExperimentalTime::class)
    suspend fun execute(
        chapter: Chapter,
        translatedContent: List<Page>,
        sourceLanguage: String,
        targetLanguage: String,
        engineId: Long
    ): Long {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
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

    /**
     * Get translation by chapter ID and target language only (ignores engine)
     */
    suspend fun getByChapterAndLanguage(
        chapterId: Long,
        targetLanguage: String
    ): TranslatedChapter? {
        return repository.getTranslatedChapterByLanguage(chapterId, targetLanguage)
    }

    /**
     * Get all translations for a chapter regardless of engine or language
     */
    suspend fun getAllForChapter(chapterId: Long): List<TranslatedChapter> {
        return repository.getAllTranslationsForChapter(chapterId)
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

        // Sort by length descending to replace longer terms first (prevents partial replacements)
        val sortedEntries = glossaryMap.entries.sortedByDescending { it.key.length }

        for ((source, target) in sortedEntries) {
            if (source.isBlank()) continue

            try {
                // Try with word boundaries first (works for Latin scripts)
                val withBoundaries = Regex("\\b${Regex.escape(source)}\\b", RegexOption.IGNORE_CASE)
                if (withBoundaries.containsMatchIn(result)) {
                    result = withBoundaries.replace(result, target)
                } else {
                    // Fallback: simple case-insensitive replacement (works for all scripts)
                    result = result.replace(source, target, ignoreCase = true)
                }
            } catch (e: Exception) {
                // If regex fails, use simple replacement
                result = result.replace(source, target, ignoreCase = true)
            }
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
