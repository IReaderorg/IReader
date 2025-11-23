package ireader.data.chapter

import app.cash.sqldelight.Query
import ireader.data.core.DatabaseHandler
import ireader.data.util.toDB
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.flow.Flow


class ChapterRepositoryImpl(private val handler: DatabaseHandler,) :
    ChapterRepository {
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
    }


    override suspend fun findChaptersByBookId(bookId: Long): List<Chapter> {
        return handler.awaitList {
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

}
