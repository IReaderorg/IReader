package ireader.data.history

import androidx.paging.PagingSource
import ir.kazemcodes.infinityreader.Database
import ireader.domain.models.entities.HistoryWithRelations
import ireader.data.core.AndroidDatabaseHandler
import ireader.data.core.DatabaseHandler
import ireader.data.util.toDB
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.models.entities.History
import kotlinx.coroutines.flow.Flow


// I may need to add bookId as Primary Key instead of using id

class HistoryRepositoryImpl constructor(
    private val handler: AndroidDatabaseHandler,

    ) :
    HistoryRepository {
    override suspend fun findHistory(id: Long): History? {
        return handler.awaitOneOrNull { historyQueries.findHistoryByBookId(id, historyMapper) }
    }

    override suspend fun findHistoryByBookId(bookId: Long): History? {
        return handler.awaitOneOrNull { historyQueries.findHistoryByBookId(bookId, historyMapper) }
    }

    override suspend fun findHistoriesByBookId(bookId: Long): List<History> {
        return handler.awaitList { historyQueries.findHistoryByBookId(bookId, historyMapper) }
    }

    override fun subscribeHistoryByBookId(bookId: Long): Flow<History?> {
        return handler.subscribeToOneOrNull { historyQueries.findHistoryByBookId(bookId, historyMapper) }
    }
    
    override fun findHistoriesPaging(query: String):
            PagingSource<Long, HistoryWithRelations> {
        return handler
            .subscribeToPagingSource(
                countQuery = { historyViewQueries.countHistory(query) },
                queryProvider = { limit, offset ->
                    historyViewQueries.history(query,limit,offset,historyWithRelationsMapper)
                },
            )
    }

    override suspend fun findHistories(): List<History> {
        return handler
            .awaitList { historyQueries.findHistories(historyMapper) }
    }

    override suspend fun insertHistory(history: History) {
        return handler.await { insertBlocking(history) }
    }

    override suspend fun insertHistories(histories: List<History>) {
        return handler.await(inTransaction = true) {
            for (i in histories) {
                insertBlocking(i)
            }
        }
    }

    override suspend fun deleteHistories(histories: List<History>) {
        return handler.await(inTransaction = true) {
            for (i in histories) {
                historyQueries.deleteByBookId(i.id)
            }
        }
    }

    override suspend fun deleteHistory(chapterId: Long) {
        return handler.await { historyQueries.deleteHistoryByChapterId(chapterId) }
    }

    override suspend fun deleteHistoryByBookId(bookId: Long) {
        return handler.await {
            historyQueries.deleteByBookId(bookId)
        }
    }

    override suspend fun deleteAllHistories() {
        return handler.await { historyQueries.deleteAllHistories() }
    }

    private fun Database.insertBlocking(history: History) {
        historyQueries.upsert(
            chapterId = history.chapterId.toDB()?:0,
            readAt = history.readAt,
            time_read = history.readDuration,
        )
    }

    private fun Database.updateBlocking(history: History) {
        historyQueries.update(
            chapter_id = history.chapterId,
            readAt = history.readAt,
            progress = history.readDuration.toLong(),
        )
    }
}
