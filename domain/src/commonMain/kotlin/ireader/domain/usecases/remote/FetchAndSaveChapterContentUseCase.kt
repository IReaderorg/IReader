package ireader.domain.usecases.remote

import ireader.core.source.model.CommandList
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.toChapterInfo
import ireader.domain.usecases.local.chapter_usecases.FindChapterById
import ireader.domain.utils.exceptionHandler
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.domain.utils.extensions.ioDispatcher
import ireader.i18n.SourceNotFoundException
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.cant_get_content
import kotlinx.coroutines.withContext

/**
 * Use case for fetching chapter content from remote source, saving to DB,
 * and returning the filtered content.
 * 
 * This ensures:
 * 1. Content is always saved to DB first
 * 2. Content is read back through FindChapterById which applies filtering
 * 3. Consistent filtering across all content access paths
 * 4. Handles both new chapters (id=0) and existing chapters correctly
 */
class FetchAndSaveChapterContentUseCase(
    private val chapterRepository: ChapterRepository,
    private val findChapterById: FindChapterById
) {
    /**
     * Fetch chapter content from remote, save to DB, and return filtered chapter.
     * 
     * @param chapter The chapter to fetch content for
     * @param catalog The catalog/source to fetch from
     * @param onSuccess Called with the filtered chapter after successful fetch and save
     * @param onError Called with error message if fetch fails
     * @param commands Optional commands for the source
     */
    suspend operator fun invoke(
        chapter: Chapter,
        catalog: CatalogLocal?,
        onSuccess: suspend (chapter: Chapter) -> Unit,
        onError: suspend (message: UiText?) -> Unit,
        commands: CommandList = emptyList()
    ) {
        withContext(ioDispatcher) {
            try {
                val source = catalog?.source ?: throw SourceNotFoundException()
                
                val pages = source.getPageList(chapter.toChapterInfo(), commands)
                
                if (pages.isEmpty()) {
                    onError(UiText.MStringResource(Res.string.cant_get_content))
                    return@withContext
                }
                
                // Create updated chapter with fetched content
                val updatedChapter = chapter.copy(
                    content = pages,
                    dateFetch = currentTimeToLong()
                )
                
                // Debug logging
                ireader.core.log.Log.debug { 
                    "FetchAndSaveChapterContent: Saving chapter id=${chapter.id}, key=${chapter.key}, bookId=${chapter.bookId}, contentSize=${pages.size}" 
                }
                
                // Save to database and get the returned ID
                // Note: For existing chapters (id != 0), the upsert updates based on (book_id, url)
                // and LAST_INSERT_ROWID() may not return the correct ID for UPDATE operations
                val returnedId = chapterRepository.insertChapter(updatedChapter)
                
                // Determine the correct ID to use for reading back
                // Always prefer the original chapter ID if it's non-zero (existing chapter)
                // Only use returnedId if the original ID was 0 (new chapter)
                val effectiveId = if (chapter.id != 0L) {
                    chapter.id  // Existing chapter - use original ID
                } else if (returnedId != 0L) {
                    returnedId  // New chapter - use database-generated ID
                } else {
                    0L  // Fallback
                }
                
                ireader.core.log.Log.debug { 
                    "FetchAndSaveChapterContent: After save - chapter.id=${chapter.id}, returnedId=$returnedId, effectiveId=$effectiveId" 
                }
                
                // Read back from DB to get filtered content and confirm save
                val filteredChapter = if (effectiveId != 0L) {
                    val result = findChapterById(effectiveId)
                    ireader.core.log.Log.debug { 
                        "FetchAndSaveChapterContent: Read back chapter id=$effectiveId, found=${result != null}, hasContent=${result?.content?.isNotEmpty() ?: false}" 
                    }
                    result
                } else {
                    null
                }
                
                if (filteredChapter != null) {
                    onSuccess(filteredChapter)
                } else {
                    // Fallback: use updated chapter with the correct ID
                    ireader.core.log.Log.warn { 
                        "FetchAndSaveChapterContent: Could not read back chapter with id=$effectiveId, using fallback" 
                    }
                    onSuccess(updatedChapter.copy(id = effectiveId))
                }
                
            } catch (e: Throwable) {
                ireader.core.log.Log.error("FetchAndSaveChapterContent: Error saving chapter", e)
                onError(exceptionHandler(e))
            }
        }
    }
}
