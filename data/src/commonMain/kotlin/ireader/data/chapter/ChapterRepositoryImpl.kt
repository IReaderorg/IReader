package ireader.data.chapter

import app.cash.sqldelight.Query
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
        // Invalidate cache for the affected book
        dbOptimizations?.invalidateCache("book_${chapter.bookId}_chapters")
        
        return handler.awaitOneAsync(inTransaction = true) {
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
    }

    override suspend fun insertChapters(chapters: List<Chapter>): List<Long> {
        if (chapters.isEmpty()) return emptyList()
        
        // Invalidate cache for affected books
        val bookIds = chapters.map { it.bookId }.distinct()
        bookIds.forEach { bookId ->
            dbOptimizations?.invalidateCache("book_${bookId}_chapters")
        }
        
        return handler.awaitListAsync(true) {
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
    }


    override suspend fun deleteChaptersByBookId(bookId: Long) {
        return handler.await {
            chapterQueries.deleteChaptersByBookId(bookId)
        }
    }

    override suspend fun deleteChapters(chapters: List<Chapter>) {
        handler.await(inTransaction = true) {
            chapters.forEach { chapter ->
                chapterQueries.delete(chapter.id)
            }
        }
    }

    override suspend fun deleteChapter(chapter: Chapter) {
        return handler.await {

            chapterQueries.delete(chapter.id)
        }
    }


    override suspend fun deleteAllChapters() {
        return handler.await {
            chapterQueries.delelteAllChapters()
        }
    }

    override suspend fun findChaptersByBookIdWithContent(bookId: Long): List<Chapter> {
        // Use full query with content for EPUB export
        // WARNING: This can cause OOM for books with many large chapters
        return handler.awaitList {
            chapterQueries.getChaptersByMangaId(bookId, chapterMapper)
        }
    }
}
