package org.ireader.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.ireader.core.utils.Constants
import org.ireader.data.local.dao.HistoryDao
import org.ireader.domain.feature_services.io.HistoryWithRelations
import org.ireader.domain.models.entities.History
import org.ireader.domain.repository.HistoryRepository

class HistoryRepositoryImpl constructor(private val historyDao: HistoryDao) : HistoryRepository {
    override suspend fun findHistory(id: Long): History {
        return historyDao.findHistory(id)
    }

    override fun findHistoriesPaging(): Flow<PagingData<HistoryWithRelations>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE,
                maxSize = Constants.MAX_PAGE_SIZE, enablePlaceholders = true),
            pagingSourceFactory = {
                historyDao.findHistoriesPaging()
            }
        ).flow
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