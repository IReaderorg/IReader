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
 * 5. Prevents race conditions between concurrent fetch operations
 * 
 * Race Condition Fix:
 * - Uses per-chapter mutex to prevent concurrent fetches of the same chapter
 * - Deduplicates fetch requests for chapters already being fetched
 * - Ensures atomic save-and-read-back operations
 */
class FetchAndSaveChapterContentUseCase(
    private val chapterRepository: ChapterRepository,
    private val findChapterById: FindChapterById
) {
    // Mutex map to synchronize fetches per chapter
    // Key: chapter key (book_id + chapter_key)
    private val chapterFetchMutexes = mutableMapOf<String, Mutex>()
    private val mutexMapLock = Mutex()
    
    // Track ongoing fetches to deduplicate requests
    private val ongoingFetches = mutableMapOf<String, FetchResult>()
    
    private data class FetchResult(
        val chapter: Chapter?,
        val error: UiText?
    )
    /**
     * Fetch chapter content from remote, save to DB, and return filtered chapter.
     * 
     * This method is thread-safe and prevents race conditions by:
     * - Using per-chapter mutexes to serialize fetches of the same chapter
     * - Deduplicating concurrent fetch requests
     * - Ensuring atomic save-and-read-back operations
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
            // Create unique key for this chapter (bookId + chapterKey)
            val chapterKey = "${chapter.bookId}_${chapter.key}"
            
            // Get or create mutex for this chapter
            val mutex = mutexMapLock.withLock {
                chapterFetchMutexes.getOrPut(chapterKey) { Mutex() }
            }
            
            // Check if this chapter is already being fetched
            val existingFetch = mutexMapLock.withLock {
                ongoingFetches[chapterKey]
            }
            
            if (existingFetch != null) {
                // Another fetch is in progress, use its result
                ireader.core.log.Log.debug { 
                    "FetchAndSaveChapterContent: Deduplicating fetch for chapter id=${chapter.id}, key=${chapter.key}" 
                }
                
                if (existingFetch.chapter != null) {
                    onSuccess(existingFetch.chapter)
                } else {
                    onError(existingFetch.error)
                }
                return@withContext
            }
            
            // Acquire mutex to ensure only one fetch per chapter at a time
            mutex.withLock {
                try {
                    // Mark this fetch as ongoing
                    mutexMapLock.withLock {
                        ongoingFetches[chapterKey] = FetchResult(null, null)
                    }
                    
                    val source = catalog?.source ?: throw SourceNotFoundException()
                    
                    val pages = source.getPageList(chapter.toChapterInfo(), commands)
                    
                    if (pages.isEmpty()) {
                        val error = UiText.MStringResource(Res.string.cant_get_content)
                        mutexMapLock.withLock {
                            ongoingFetches[chapterKey] = FetchResult(null, error)
                        }
                        onError(error)
                        return@withLock
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
                        // Store successful result
                        mutexMapLock.withLock {
                            ongoingFetches[chapterKey] = FetchResult(filteredChapter, null)
                        }
                        onSuccess(filteredChapter)
                    } else {
                        // Fallback: use updated chapter with the correct ID
                        ireader.core.log.Log.warn { 
                            "FetchAndSaveChapterContent: Could not read back chapter with id=$effectiveId, using fallback" 
                        }
                        val fallbackChapter = updatedChapter.copy(id = effectiveId)
                        mutexMapLock.withLock {
                            ongoingFetches[chapterKey] = FetchResult(fallbackChapter, null)
                        }
                        onSuccess(fallbackChapter)
                    }
                    
                } catch (e: Throwable) {
                    ireader.core.log.Log.error("FetchAndSaveChapterContent: Error saving chapter", e)
                    val error = exceptionHandler(e)
                    mutexMapLock.withLock {
                        ongoingFetches[chapterKey] = FetchResult(null, error)
                    }
                    onError(error)
                } finally {
                    // Clean up after a delay to allow deduplication window
                    kotlinx.coroutines.delay(1000)
                    mutexMapLock.withLock {
                        ongoingFetches.remove(chapterKey)
                        chapterFetchMutexes.remove(chapterKey)
                    }
                }
            }
        }
    }
}
