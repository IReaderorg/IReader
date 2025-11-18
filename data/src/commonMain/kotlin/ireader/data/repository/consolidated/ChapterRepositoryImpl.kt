package ireader.data.repository.consolidated

import ireader.core.log.IReaderLog
import ireader.data.chapter.chapterMapper
import ireader.data.core.DatabaseHandler
import ireader.data.util.toDB
import ireader.domain.data.repository.consolidated.ChapterRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.models.errors.IReaderError
import ireader.domain.models.updates.ChapterUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * ChapterRepository implementation following Mihon's DatabaseHandler pattern.
 * 
 * This implementation supports both suspend functions and Flow-based reactive queries
 * following Mihon's subscribeToOne and subscribeToList patterns.
 */
class ChapterRepositoryImpl(
    private val handler: DatabaseHandler,
) : ChapterRepository {

    override suspend fun getChapterById(id: Long): Chapter? {
        return try {
            handler.awaitOneOrNull { 
                chapterQueries.getChapterById(id, chapterMapper) 
            }
        } catch (e: Exception) {
            IReaderLog.error("Failed to get chapter by id: $id", e, "ChapterRepository")
            null
        }
    }

    override fun getChapterByIdAsFlow(id: Long): Flow<Chapter?> {
        return handler.subscribeToOneOrNull { 
            chapterQueries.getChapterById(id, chapterMapper) 
        }.catch { e ->
            IReaderLog.error("Failed to subscribe to chapter: $id", e, "ChapterRepository")
            emit(null)
        }
    }

    override suspend fun getChaptersByBookId(bookId: Long): List<Chapter> {
        return try {
            handler.awaitList { 
                chapterQueries.getChaptersByMangaId(bookId, chapterMapper) 
            }
        } catch (e: Exception) {
            IReaderLog.error("Failed to get chapters for book: $bookId", e, "ChapterRepository")
            emptyList()
        }
    }

    override fun getChaptersByBookIdAsFlow(bookId: Long): Flow<List<Chapter>> {
        return handler.subscribeToList { 
            chapterQueries.getChaptersByMangaId(bookId, chapterMapper) 
        }.catch { e ->
            IReaderLog.error("Failed to subscribe to chapters for book: $bookId", e, "ChapterRepository")
            emit(emptyList())
        }
    }

    override suspend fun getLastReadChapter(bookId: Long): Chapter? {
        return try {
            handler.awaitOneOrNull { 
                chapterQueries.getLastChapter(bookId, chapterMapper) 
            }
        } catch (e: Exception) {
            IReaderLog.error("Failed to get last read chapter for book: $bookId", e, "ChapterRepository")
            null
        }
    }

    override fun getLastReadChapterAsFlow(bookId: Long): Flow<Chapter?> {
        return handler.subscribeToOneOrNull { 
            chapterQueries.getLastChapter(bookId, chapterMapper) 
        }.catch { e ->
            IReaderLog.error("Failed to subscribe to last read chapter for book: $bookId", e, "ChapterRepository")
            emit(null)
        }
    }

    override suspend fun addAll(chapters: List<Chapter>): List<Chapter> {
        return try {
            handler.awaitListAsync(inTransaction = true) {
                chapters.forEach { chapter ->
                    chapterQueries.upsert(
                        id = chapter.id.toDB(),
                        bookId = chapter.bookId,
                        key = chapter.key,
                        name = chapter.name,
                        read = chapter.read,
                        bookmark = chapter.bookmark,
                        last_page_read = chapter.lastPageRead,
                        chapter_number = chapter.number,
                        source_order = chapter.sourceOrder,
                        date_fetch = chapter.dateFetch,
                        date_upload = chapter.dateUpload,
                        translator = chapter.translator,
                        type = chapter.type,
                        content = chapter.content
                    )
                }
                chapterQueries.selectLastInsertedRowId()
            }
            IReaderLog.debug("Successfully inserted ${chapters.size} chapters", "ChapterRepository")
            chapters
        } catch (e: Exception) {
            IReaderLog.error("Failed to insert ${chapters.size} chapters", e, "ChapterRepository")
            emptyList()
        }
    }

    override suspend fun update(update: ChapterUpdate): Boolean {
        return try {
            partialUpdate(update)
            IReaderLog.debug("Successfully updated chapter: ${update.id}", "ChapterRepository")
            true
        } catch (e: Exception) {
            IReaderLog.error("Failed to update chapter: ${update.id}", e, "ChapterRepository")
            false
        }
    }

    override suspend fun updateAll(updates: List<ChapterUpdate>): Boolean {
        return try {
            handler.await(inTransaction = true) {
                updates.forEach { update ->
                    partialUpdate(update)
                }
            }
            IReaderLog.debug("Successfully updated ${updates.size} chapters", "ChapterRepository")
            true
        } catch (e: Exception) {
            IReaderLog.error("Failed to update ${updates.size} chapters", e, "ChapterRepository")
            false
        }
    }

    override suspend fun removeChaptersWithIds(chapterIds: List<Long>): Boolean {
        return try {
            handler.await(inTransaction = true) {
                chapterIds.forEach { chapterId ->
                    chapterQueries.delete(chapterId)
                }
            }
            IReaderLog.debug("Successfully removed ${chapterIds.size} chapters", "ChapterRepository")
            true
        } catch (e: Exception) {
            IReaderLog.error("Failed to remove ${chapterIds.size} chapters", e, "ChapterRepository")
            false
        }
    }

    override suspend fun removeChaptersByBookId(bookId: Long): Boolean {
        return try {
            handler.await { 
                chapterQueries.deleteChaptersByBookId(bookId) 
            }
            IReaderLog.debug("Successfully removed chapters for book: $bookId", "ChapterRepository")
            true
        } catch (e: Exception) {
            IReaderLog.error("Failed to remove chapters for book: $bookId", e, "ChapterRepository")
            false
        }
    }

    private suspend fun partialUpdate(update: ChapterUpdate) {
        // Get existing chapter and merge updates
        val existing = getChapterById(update.id) ?: return
        
        handler.await {
            chapterQueries.update(
                chapterId = update.id,
                mangaId = update.bookId ?: existing.bookId,
                url = update.key ?: existing.key,
                name = update.name ?: existing.name,
                scanlator = update.translator ?: existing.translator,
                read = update.read ?: existing.read,
                bookmark = update.bookmark ?: existing.bookmark,
                lastPageRead = update.lastPageRead ?: existing.lastPageRead,
                chapterNumber = (update.number ?: existing.number).toDouble(),
                sourceOrder = update.sourceOrder ?: existing.sourceOrder,
                dateFetch = update.dateFetch ?: existing.dateFetch,
                dateUpload = update.dateUpload ?: existing.dateUpload
            )
        }
    }
}