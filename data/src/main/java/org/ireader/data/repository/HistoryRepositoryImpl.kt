package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
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

    override fun findHistoriesPaging(): Flow<List<HistoryWithRelations>> {
        return historyDao.findHistoriesPaging()
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

    override suspend fun deleteHistory(history: History) {
        return historyDao.deleteHistory(history)
    }

}