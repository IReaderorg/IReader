package ireader.domain.usecases.chapter.controller

import ireader.core.source.model.CommandList
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.toChapterInfo
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.domain.utils.extensions.ioDispatcher
import ireader.i18n.SourceNotFoundException
import kotlinx.coroutines.withContext

/**
 * Use case interface for loading chapter content.
 * Handles both local content retrieval and remote fetching.
 */
interface LoadChapterContentUseCase {
    /**
     * Load content for a chapter. If content is not available locally,
     * fetches from the remote source.
     *
     * @param chapter The chapter to load content for
     * @param catalog The catalog/source to fetch from if needed
     * @param commands Optional commands for the source
     * @return Result containing the chapter with content, or failure
     */
    suspend fun loadContent(
        chapter: Chapter,
        catalog: CatalogLocal?,
        commands: CommandList = emptyList()
    ): Result<Chapter>

    /**
     * Preload content for a chapter in the background.
     * Similar to loadContent but intended for background preloading.
     *
     * @param chapter The chapter to preload content for
     * @param catalog The catalog/source to fetch from if needed
     * @param commands Optional commands for the source
     * @return Result containing the chapter with content, or failure
     */
    suspend fun preloadContent(
        chapter: Chapter,
        catalog: CatalogLocal?,
        commands: CommandList = emptyList()
    ): Result<Chapter>
}

/**
 * Default implementation of [LoadChapterContentUseCase].
 * Fetches content from remote source and persists to local database.
 */
class LoadChapterContentUseCaseImpl(
    private val chapterRepository: ChapterRepository
) : LoadChapterContentUseCase {

    override suspend fun loadContent(
        chapter: Chapter,
        catalog: CatalogLocal?,
        commands: CommandList
    ): Result<Chapter> = withContext(ioDispatcher) {
        runCatching {
            // If chapter already has content, return it
            if (chapter.content.isNotEmpty()) {
                return@runCatching chapter
            }

            // Try to get from database first (might have been downloaded)
            val dbChapter = chapterRepository.findChapterById(chapter.id)
            if (dbChapter != null && dbChapter.content.isNotEmpty()) {
                return@runCatching dbChapter
            }

            // Fetch from remote source
            val source = catalog?.source ?: throw SourceNotFoundException()
            
            val pages = source.getPageList(chapter.toChapterInfo(), commands)
            
            if (pages.isEmpty()) {
                throw ContentLoadException("Failed to load content: empty response")
            }

            val updatedChapter = chapter.copy(
                content = pages,
                dateFetch = currentTimeToLong()
            )

            // Persist to database
            chapterRepository.insertChapter(updatedChapter)

            updatedChapter
        }
    }

    override suspend fun preloadContent(
        chapter: Chapter,
        catalog: CatalogLocal?,
        commands: CommandList
    ): Result<Chapter> {
        // Preload uses the same logic as load
        return loadContent(chapter, catalog, commands)
    }
}

/**
 * Exception thrown when content loading fails.
 */
class ContentLoadException(message: String, cause: Throwable? = null) : Exception(message, cause)
