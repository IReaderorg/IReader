package ireader.data.history

import androidx.paging.PagingSource
import ir.kazemcodes.infinityreader.Database
import ireader.domain.data.repository.HistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.History
import ireader.common.models.entities.HistoryWithRelations
import ireader.data.local.DatabaseHandler
import ireader.data.util.toDB
import java.util.*


class HistoryRepositoryImpl constructor(
    private val handler: DatabaseHandler,

    ) :
    HistoryRepository {
    override suspend fun findHistory(id: Long): ireader.common.models.entities.History? {
        return handler.awaitOneOrNull { historyQueries.findHistoryByBookId(id, historyMapper) }
    }

    override suspend fun findHistoryByBookId(bookId: Long): ireader.common.models.entities.History? {
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

    override suspend fun findHistories(): List<ireader.common.models.entities.History> {
        return handler
            .awaitList { historyQueries.findHistories(historyMapper) }
    }

    override suspend fun insertHistory(history: ireader.common.models.entities.History) {
        return handler.await { insertBlocking(history) }
    }

    override suspend fun insertHistories(histories: List<ireader.common.models.entities.History>) {
        return handler.await(inTransaction = true) {
            for (i in histories) {
                insertBlocking(i)
            }
        }
    }

    override suspend fun deleteHistories(histories: List<ireader.common.models.entities.History>) {
        return handler.await(inTransaction = true) {
            for (i in histories) {
                historyQueries.deleteHistoryByBookId(i.id)
            }
        }
    }

    override suspend fun deleteHistory(chapterId: Long) {
        return handler.await { historyQueries.deleteHistoryByChapterId(chapterId) }
    }

    override suspend fun deleteHistoryByBookId(bookId: Long) {
        return handler.await { historyQueries.deleteHistoryByBookId(bookId) }
    }

    override suspend fun deleteAllHistories() {
        return handler.await { historyQueries.deleteAllHistories() }
    }

    private fun Database.insertBlocking(history: History) {
        historyQueries.upsert(
            chapterId = history.chapterId,
            readAt = history.readAt,
            time_read = history.readDuration,
        )
    }

    private fun Database.updateBlocking(history: History) {
        historyQueries.update(
            bookId = history.id,
            chapter_id = history.chapterId,
            readAt = history.readAt,
            progress = history.readDuration.toLong(),
        )
    }
}
