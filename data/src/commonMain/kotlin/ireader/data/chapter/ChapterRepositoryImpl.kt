package ireader.data.chapter

import ireader.core.log.Log
import ireader.data.core.DatabaseHandler
import ireader.data.core.DatabaseOptimizations
import ireader.data.util.toDB
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.time.Duration.Companion.milliseconds


/**
 * Repository implementation for chapter data operations.
 * 
 * Note: ChapterNotifier is NOT injected here. All change notifications are emitted
 * by ChapterController (single source of truth) to avoid duplicate notifications.
 */
@OptIn(FlowPreview::class)
class ChapterRepositoryImpl(
    private val handler: DatabaseHandler,
    private val dbOptimizations: DatabaseOptimizations? = null
) : ChapterRepository {
    override fun subscribeChapterById(chapterId: Long): Flow<Chapter?> {
        return handler.subscribeToOneOrNull {
            chapterQueries.getChapterById(chapterId, chapterMapper)
        }
    }

    override suspend fun findChapterById(chapterId: Long): Chapter? {
        return handler.awaitOneOrNull {
            chapterQueries.getChapterById(chapterId, chapterMapper)
        }
    }

    override suspend fun findAllChapters(): List<Chapter> {
        return handler.awaitList {
            // Use lightweight query to prevent OOM errors
            chapterQueries.findAllLight(chapterMapperLight)
        }
    }

    override suspend fun findAllInLibraryChapter(): List<Chapter> {
        return handler.awaitList {
            // Use lightweight query to prevent OOM errors
            chapterQueries.findInLibraryLight(chapterMapperLight)
        }
    }

    override fun subscribeChaptersByBookId(bookId: Long): Flow<List<Chapter>> {
        return handler.subscribeToList {
            // Use lightweight query to prevent OOM errors when observing chapter lists
            chapterQueries.getChaptersByMangaIdLight(bookId, chapterMapperLight)
        }
            .debounce(100.milliseconds) // Prevent rapid emissions during batch updates
            .distinctUntilChanged() // Skip duplicate emissions
    }


    override suspend fun findChaptersByBookId(bookId: Long): List<Chapter> {
        // Use cached query for chapter lists (frequently accessed when viewing book details)
        return dbOptimizations?.awaitListCached(
            cacheKey = "book_${bookId}_chapters",
            ttl = DatabaseOptimizations.SHORT_CACHE_TTL
        ) {
            chapterQueries.getChaptersByMangaIdLight(bookId, chapterMapperLight)
        } ?: handler.awaitList {
            // Use lightweight query to prevent OOM errors when listing chapters
            chapterQueries.getChaptersByMangaIdLight(bookId, chapterMapperLight)
        }
    }


    override suspend fun findLastReadChapter(bookId: Long): Chapter? {
        return handler.awaitOneOrNull {
            chapterQueries.getLastChapter(
                bookId,
                chapterMapper
            )

        }
    }

    override suspend fun subscribeLastReadChapter(bookId: Long): Flow<Chapter?> {
        return handler.subscribeToOneOrNull {
            chapterQueries.getLastChapter(
                    bookId,
                    chapterMapper
            )

        }
    }


    override suspend fun insertChapter(chapter: Chapter): Long {
        dbOptimizations?.invalidateCache("book_${chapter.bookId}_chapters")
        val result = handler.awaitOneAsync(inTransaction = true) {
                chapterQueries.upsert(
                    chapter.id.toDB(),
                    chapter.bookId,
                    chapter.key,
                    chapter.name,
                    chapter.translator,
                    chapter.read,
                    chapter.bookmark,
                    chapter.lastPageRead,
                    chapter.number,
                    chapter.sourceOrder,
                    chapter.dateFetch,
                    chapter.dateUpload,
                    chapter.content,
                    chapter.type,
                )
             chapterQueries.selectLastInsertedRowId()
        }
        
        // Invalidate cache AFTER the insert to ensure fresh data is read
        // This double invalidation prevents race conditions where another thread
        // might read and cache stale data between the insert and invalidation
        dbOptimizations?.invalidateCache("book_${chapter.bookId}_chapters")
        
        // Refresh cached chapter counts for the book (used by smart categories)
        handler.await {
            chapterQueries.refreshBookChapterCounts(chapter.bookId)
        }
        
        return result
    }

    override suspend fun insertChapters(chapters: List<Chapter>): List<Long> {
        val tag = "ChapterRepositoryImpl"
        
        if (chapters.isEmpty()) return emptyList()
        
        // Log batch insert
        Log.debug { 
            "$tag: insertChapters called with ${chapters.size} chapters. " +
            "Empty content chapters: ${chapters.count { it.content.isEmpty() }}"
        }
        
        // Check for chapters with empty content that might overwrite existing content
        val chaptersWithEmptyContent = chapters.filter { it.content.isEmpty() }
        if (chaptersWithEmptyContent.isNotEmpty()) {
            Log.warn { 
                "$tag: WARNING - ${chaptersWithEmptyContent.size} chapters have empty content and may overwrite existing data. " +
                "Chapter IDs: ${chaptersWithEmptyContent.take(5).map { it.id }}"
            }
        }
        
        val bookIds = chapters.map { it.bookId }.distinct()
        
        // Invalidate cache BEFORE the insert to prevent stale reads during insert
        bookIds.forEach { bookId ->
            dbOptimizations?.invalidateCache("book_${bookId}_chapters")
        }
        
        val result = handler.awaitListAsync(true) {
            chapters.forEach { chapter ->
                chapterQueries.upsert(
                    chapter.id.toDB(),
                    chapter.bookId,
                    chapter.key,
                    chapter.name,
                    chapter.translator,
                    chapter.read,
                    chapter.bookmark,
                    chapter.lastPageRead,
                    chapter.number,
                    chapter.sourceOrder,
                    chapter.dateFetch,
                    chapter.dateUpload,
                    chapter.content,
                    chapter.type
                )
            }
            chapterQueries.selectLastInsertedRowId()
        }
        
        // Invalidate cache AFTER the insert to ensure fresh data is read
        // This double invalidation prevents race conditions where another thread
        // might read and cache stale data between the insert and invalidation
        bookIds.forEach { bookId ->
            dbOptimizations?.invalidateCache("book_${bookId}_chapters")
        }
        
        // Refresh cached chapter counts for affected books (used by smart categories)
        handler.await {
            bookIds.forEach { bookId ->
                chapterQueries.refreshBookChapterCounts(bookId)
            }
        }
        
        return result
    }


    override suspend fun deleteChaptersByBookId(bookId: Long) {
        handler.await {
            chapterQueries.deleteChaptersByBookId(bookId)
        }
        
        // Refresh cached chapter counts for the book (used by smart categories)
        handler.await {
            chapterQueries.refreshBookChapterCounts(bookId)
        }
    }

    override suspend fun deleteChapters(chapters: List<Chapter>) {
        if (chapters.isEmpty()) return
        
        val bookIds = chapters.map { it.bookId }.distinct()
        
        handler.await(inTransaction = true) {
            chapters.forEach { chapter ->
                chapterQueries.delete(chapter.id)
            }
        }
        
        // Refresh cached chapter counts for affected books (used by smart categories)
        handler.await {
            bookIds.forEach { bookId ->
                chapterQueries.refreshBookChapterCounts(bookId)
            }
        }
    }

    override suspend fun deleteChapter(chapter: Chapter) {
        handler.await {
            chapterQueries.delete(chapter.id)
        }
        
        // Refresh cached chapter counts for the book (used by smart categories)
        handler.await {
            chapterQueries.refreshBookChapterCounts(chapter.bookId)
        }
    }


    override suspend fun deleteAllChapters() {
        handler.await {
            chapterQueries.delelteAllChapters()
        }
    }
    
    override suspend fun updateLastPageRead(chapterId: Long, lastPageRead: Long) {
        handler.await {
            chapterQueries.updateLastPageRead(lastPageRead, chapterId)
        }
    }

    override suspend fun findChaptersByBookIdWithContent(bookId: Long): List<Chapter> {
        // Use full query with content for EPUB export
        // WARNING: This can cause OOM for books with many large chapters
        return handler.awaitList {
            chapterQueries.getChaptersByMangaId(bookId, chapterMapper)
        }
    }
    
    override suspend fun clearChapterContent(chapterIds: List<Long>) {
        if (chapterIds.isEmpty()) return
        
        Log.debug { "ChapterRepositoryImpl: clearChapterContent called for ${chapterIds.size} chapters" }
        
        handler.await {
            chapterQueries.clearChapterContent(chapterIds)
        }
    }
}
