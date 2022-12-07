package ireader.data.chapter

import ireader.domain.models.entities.Chapter
import ireader.data.local.DatabaseHandler
import ireader.data.util.toDB
import ireader.domain.data.repository.ChapterRepository
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
            chapterQueries.findAll(chapterMapper)
        }
    }

    override suspend fun findAllInLibraryChapter(): List<Chapter> {
        return handler.awaitList {
            chapterQueries.findInLibrary(chapterMapper)
        }
    }

    override fun subscribeChaptersByBookId(bookId: Long): Flow<List<Chapter>> {
        return handler.subscribeToList {
            chapterQueries.getChaptersByMangaId(bookId, chapterMapper)
        }
    }


    override suspend fun findChaptersByBookId(bookId: Long): List<Chapter> {
        return handler.awaitList {
            chapterQueries.getChaptersByMangaId(bookId, chapterMapper)
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
        return handler.awaitOne {
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
        return handler.awaitList(true) {
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
                content = chapter.content,
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
