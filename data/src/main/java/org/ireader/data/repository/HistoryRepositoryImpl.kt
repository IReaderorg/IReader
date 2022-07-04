package org.ireader.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import org.ireader.common_models.entities.History
import org.ireader.common_models.entities.HistoryWithRelations
import org.ireader.data.local.dao.HistoryDao

class HistoryRepositoryImpl constructor(private val historyDao: HistoryDao) :
    org.ireader.common_data.repository.HistoryRepository {
    override suspend fun findHistory(id: Long): org.ireader.common_models.entities.History? {
        return historyDao.findHistory(id)
    }

    override suspend fun findHistoryByBookId(bookId: Long): org.ireader.common_models.entities.History? {
        return historyDao.findHistoryByBookId(bookId)
    }

    override suspend fun findHistoriesByBookId(bookId: Long): List<History> {
        return historyDao.findHistoriesByBookId(bookId)
    }

    override fun subscribeHistoryByBookId(bookId: Long): Flow<History?> {
        return historyDao.subscribeHistoryByBookId(bookId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun findHistoriesPaging(query: String):
        Flow<Map<LocalDate, List<HistoryWithRelations>>> {
        return historyDao.findHistoriesPaging(query).mapLatest { histories ->
            histories.distinctBy { it.bookId }.groupBy { history -> history.date.toLocalDate() }
        }
    }

    override suspend fun findHistories(): List<org.ireader.common_models.entities.History> {
        return historyDao.findHistories()
    }

    override suspend fun insertHistory(history: org.ireader.common_models.entities.History) {
        return historyDao.insertOrUpdate(history)
    }

    override suspend fun insertHistories(histories: List<org.ireader.common_models.entities.History>){
        return historyDao.insertOrUpdate(histories)
    }

    override suspend fun deleteHistories(histories: List<org.ireader.common_models.entities.History>) {
        return historyDao.delete(histories)
    }

    override suspend fun deleteHistory(chapterId: Long) {
        return historyDao.deleteHistory(chapterId)
    }

    override suspend fun deleteHistoryByBookId(bookId: Long) {
        return historyDao.deleteHistoryByBooKId(bookId)
    }

    override suspend fun deleteAllHistories() {
        return historyDao.deleteAllHistory()
    }
}
