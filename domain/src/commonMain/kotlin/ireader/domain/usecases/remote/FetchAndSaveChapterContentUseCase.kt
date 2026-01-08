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
                
                // Save to database
                chapterRepository.insertChapter(updatedChapter)
                
                // Read back from DB to get filtered content
                val filteredChapter = findChapterById(chapter.id)
                
                if (filteredChapter != null) {
                    onSuccess(filteredChapter)
                } else {
                    onSuccess(updatedChapter)
                }
                
            } catch (e: Throwable) {
                onError(exceptionHandler(e))
            }
        }
    }
}
