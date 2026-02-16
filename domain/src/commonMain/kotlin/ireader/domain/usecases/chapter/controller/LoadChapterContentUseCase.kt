package ireader.domain.usecases.chapter.controller

import ireader.core.log.Log
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

    companion object {
        private const val TAG = "LoadChapterContentUseCase"
    }

    override suspend fun loadContent(
        chapter: Chapter,
        catalog: CatalogLocal?,
        commands: CommandList
    ): Result<Chapter> = withContext(ioDispatcher) {
        runCatching {
            Log.debug { "$TAG: loadContent called for chapter id=${chapter.id}, key=${chapter.key}, bookId=${chapter.bookId}, hasContent=${chapter.content.isNotEmpty()}" }
            
            // If chapter already has content, return it
            if (chapter.content.isNotEmpty()) {
                Log.debug { "$TAG: Chapter already has content, returning without fetch" }
                return@runCatching chapter
            }

            // Try to get from database first (might have been downloaded)
            val dbChapter = chapterRepository.findChapterById(chapter.id)
            if (dbChapter != null && dbChapter.content.isNotEmpty()) {
                Log.debug { "$TAG: Found content in DB for chapter id=${chapter.id}, contentSize=${dbChapter.content.size}" }
                return@runCatching dbChapter
            }

            // Fetch from remote source
            val source = catalog?.source ?: throw SourceNotFoundException()
            
            Log.debug { "$TAG: Fetching from remote for chapter id=${chapter.id}" }
            val pages = source.getPageList(chapter.toChapterInfo(), commands)
            
            Log.debug { "$TAG: Remote fetch returned ${pages.size} pages for chapter id=${chapter.id}" }
            
            if (pages.isEmpty()) {
                Log.warn { "$TAG: Remote fetch returned EMPTY content for chapter id=${chapter.id}" }
                throw ContentLoadException("Failed to load content: empty response")
            }

            val updatedChapter = chapter.copy(
                content = pages,
                dateFetch = currentTimeToLong()
            )

            Log.debug { "$TAG: Inserting chapter id=${chapter.id} with contentSize=${pages.size} to database" }
            
            // Persist to database
            val insertedId = chapterRepository.insertChapter(updatedChapter)
            
            Log.debug { "$TAG: Insert returned id=$insertedId for chapter id=${chapter.id}" }

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
