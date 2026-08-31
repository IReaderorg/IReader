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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
 * 5. Prevents race conditions between concurrent fetch operations via CompletableDeferred deduplication
 */
class FetchAndSaveChapterContentUseCase(
    private val chapterRepository: ChapterRepository,
    private val findChapterById: FindChapterById
) {
    // Track ongoing fetches with CompletableDeferred to seamlessly deduplicate concurrent requests
    private val ongoingFetches = mutableMapOf<String, CompletableDeferred<Chapter>>()
    private val fetchMapLock = Mutex()

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
            val chapterKey = "${chapter.bookId}_${chapter.key}"
            
            // Check or register ongoing fetch
            val (deferred, isInitiator) = fetchMapLock.withLock {
                val existing = ongoingFetches[chapterKey]
                if (existing != null) {
                    existing to false
                } else {
                    val newDeferred = CompletableDeferred<Chapter>()
                    ongoingFetches[chapterKey] = newDeferred
                    newDeferred to true
                }
            }
            
            if (!isInitiator) {
                ireader.core.log.Log.debug { 
                    "FetchAndSaveChapterContent: Awaiting concurrent ongoing fetch for chapter id=${chapter.id}, key=${chapter.key}" 
                }
                try {
                    val result = deferred.await()
                    onSuccess(result)
                } catch (e: Throwable) {
                    onError(exceptionHandler(e))
                }
                return@withContext
            }
            
            try {
                val source = catalog?.source ?: throw SourceNotFoundException()
                val pages = source.getPageList(chapter.toChapterInfo(), commands)
                
                if (pages.isEmpty()) {
                    val error = UiText.MStringResource(Res.string.cant_get_content)
                    deferred.completeExceptionally(Exception("Empty chapter content received"))
                    onError(error)
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
                val returnedId = chapterRepository.insertChapter(updatedChapter)
                val effectiveId = if (chapter.id != 0L) {
                    chapter.id  // Existing chapter - use original ID
                } else if (returnedId != 0L) {
                    returnedId  // New chapter - use database-generated ID
                } else {
                    0L  // Fallback
                }
                
                // Read back from DB to get filtered content and confirm save
                val filteredChapter = if (effectiveId != 0L) {
                    findChapterById(effectiveId)
                } else {
                    null
                } ?: updatedChapter.copy(id = effectiveId)
                
                deferred.complete(filteredChapter)
                onSuccess(filteredChapter)
            } catch (e: Throwable) {
                ireader.core.log.Log.error("FetchAndSaveChapterContent: Error saving chapter", e)
                deferred.completeExceptionally(e)
                onError(exceptionHandler(e))
            } finally {
                fetchMapLock.withLock {
                    ongoingFetches.remove(chapterKey)
                }
            }
        }
    }
}

