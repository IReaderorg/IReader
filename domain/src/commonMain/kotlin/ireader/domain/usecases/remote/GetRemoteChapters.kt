package ireader.domain.usecases.remote
import ireader.domain.utils.extensions.ioDispatcher

import ireader.core.log.Log
import ireader.core.source.model.ChaptersPageInfo
import ireader.core.source.model.CommandList
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Book.Companion.toBookInfo
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.isSameChapter
import ireader.domain.models.entities.toChapters
import ireader.domain.utils.exceptionHandler
import ireader.i18n.SourceNotFoundException
import ireader.i18n.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class GetRemoteChapters() {
    suspend operator fun invoke(
            book: Book,
            catalog: CatalogLocal?,
            commands: CommandList = emptyList(),
            oldChapters: List<Chapter>,
            onSuccess: suspend (List<Chapter>) -> Unit,
            onRemoteSuccess: suspend (List<Chapter>) -> Unit = {},
            onError: suspend (UiText?) -> Unit,
    ) {
        withContext(ioDispatcher) {
            kotlin.runCatching {
                try {
                    val source = catalog?.source ?: throw SourceNotFoundException()
                    Log.debug { "Timber: GetRemoteChaptersUseCase was Called" }

                    val fetchedChapters = source.getChapterList(manga = book.toBookInfo(), commands)
                            .toChapters(book.id)
                    onRemoteSuccess(fetchedChapters)

                    val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
                    val chaptersToSave = mutableListOf<Chapter>()
                    for (chapter in fetchedChapters) {
                        if (chaptersToSave.any { it.isSameChapter(chapter) }) continue
                        val existing = oldChapters.firstOrNull { it.isSameChapter(chapter) }
                        chaptersToSave += when {
                            existing != null -> existing.copy(
                                key = chapter.key,
                                name = chapter.name,
                                number = chapter.number,
                                dateUpload = chapter.dateUpload,
                                sourceOrder = chapter.sourceOrder,
                                translator = chapter.translator,
                                type = chapter.type,
                            )
                            oldChapters.isEmpty() -> chapter
                            else -> chapter.copy(dateFetch = now)
                        }
                    }
                    // A downloaded chapter can disappear from the source list. Keep it,
                    // but give it a unique order so the whole TOC stops falling back to
                    // insertion order when legacy rows are tied at 0.
                    var nextOrder = (chaptersToSave.maxOfOrNull { it.sourceOrder } ?: -1L) + 1
                    for (oldChapter in oldChapters) {
                        if (chaptersToSave.any { it.isSameChapter(oldChapter) }) continue
                        chaptersToSave += oldChapter.copy(sourceOrder = nextOrder++)
                    }
                    onSuccess(chaptersToSave)
                    Log.debug { "Timber: GetRemoteChaptersUseCase was Finished Successfully" }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    onError(exceptionHandler(e))
                }
            }.getOrElse { e ->
                if (e !is CancellationException) {
                    onError(exceptionHandler(e))
                }
            }
        }
    }
    
    /**
     * Fetch a specific page of chapters from the source.
     * Use this for sources that support paginated chapter loading.
     * 
     * @param book The book to fetch chapters for
     * @param catalog The catalog/source to use
     * @param page The page number (1-based)
     * @param commands Optional commands for the request
     * @return ChapterPageResult containing chapters and pagination info
     */
    suspend fun getChapterPage(
        book: Book,
        catalog: CatalogLocal?,
        page: Int,
        commands: CommandList = emptyList(),
    ): ChapterPageResult {
        return withContext(ioDispatcher) {
            try {
                val source = catalog?.source ?: throw SourceNotFoundException()
                Log.debug { "GetRemoteChapters: Fetching page $page for book ${book.title}" }
                
                val pageInfo = source.getChapterListPaged(
                    manga = book.toBookInfo(),
                    page = page,
                    commands = commands
                )
                
                // Pages are fetched independently, so give each one a distinct
                // source-order range instead of letting every chapter tie at 0.
                val pageOffset = (page - 1L) * 1000L
                val chapters = pageInfo.chapters.toChapters(book.id)
                    .map { it.copy(sourceOrder = pageOffset + it.sourceOrder) }
                
                Log.debug { "GetRemoteChapters: Got ${chapters.size} chapters, page ${pageInfo.currentPage}/${pageInfo.totalPages}" }
                
                ChapterPageResult.Success(
                    chapters = chapters,
                    currentPage = pageInfo.currentPage,
                    totalPages = pageInfo.totalPages,
                    hasNextPage = pageInfo.hasNextPage,
                    hasPreviousPage = pageInfo.hasPreviousPage
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.error("GetRemoteChapters: Error fetching page $page", e)
                ChapterPageResult.Error(exceptionHandler(e))
            }
        }
    }
    
    /**
     * Get the total number of chapter pages for a book.
     */
    suspend fun getChapterPageCount(
        book: Book,
        catalog: CatalogLocal?
    ): Int {
        return withContext(ioDispatcher) {
            try {
                val source = catalog?.source ?: return@withContext 1
                source.getChapterPageCount(book.toBookInfo())
            } catch (e: Exception) {
                Log.error("GetRemoteChapters: Error getting page count", e)
                1
            }
        }
    }
    
    /**
     * Check if the source supports paginated chapter loading.
     */
    fun supportsPaginatedChapters(catalog: CatalogLocal?): Boolean {
        return catalog?.source?.supportsPaginatedChapters() ?: false
    }
}

/**
 * Result of fetching a page of chapters.
 */
sealed class ChapterPageResult {
    data class Success(
        val chapters: List<Chapter>,
        val currentPage: Int,
        val totalPages: Int,
        val hasNextPage: Boolean,
        val hasPreviousPage: Boolean
    ) : ChapterPageResult() {
        val isPaginated: Boolean get() = totalPages > 1
    }
    
    data class Error(val error: UiText?) : ChapterPageResult()
}
