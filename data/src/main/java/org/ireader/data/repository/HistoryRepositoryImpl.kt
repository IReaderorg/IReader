package org.ireader.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import org.ireader.data.local.dao.HistoryDao
import org.ireader.domain.feature_services.io.HistoryWithRelations
import org.ireader.domain.models.entities.History
import org.ireader.domain.repository.HistoryRepository

class HistoryRepositoryImpl constructor(private val historyDao: HistoryDao) : HistoryRepository {
    override suspend fun findHistory(id: Long): History? {
        return historyDao.findHistory(id)
    }

    override suspend fun findHistoryByBookId(bookId: Long): History? {
        return historyDao.findHistoryByBookId(bookId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun findHistoriesPaging():
            Flow<Map<String, List<HistoryWithRelations>>> {
        return historyDao.findHistoriesPaging().mapLatest { histories ->
            histories.distinctBy { it.bookId }.groupBy { history -> history.date }
        }
    }

    override suspend fun findHistories(): List<History> {
        return historyDao.findHistories()
    }

    override suspend fun insertHistory(history: History): Long {
        return historyDao.insertHistory(history)
    }

    override suspend fun insertHistories(histories: List<History>): List<Long> {
        return historyDao.insertHistories(histories)
    }

    override suspend fun deleteAllHistories(histories: List<History>) {
        return historyDao.deleteAllHistories(histories)
    }

    override suspend fun deleteHistory(chapterId: Long) {
        return historyDao.deleteHistory(chapterId)
    }

}