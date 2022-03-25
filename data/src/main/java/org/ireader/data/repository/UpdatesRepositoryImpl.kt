package org.ireader.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import org.ireader.core.utils.convertLongToTime
import org.ireader.data.local.dao.UpdatesDao
import org.ireader.domain.models.entities.Update
import org.ireader.domain.repository.UpdatesRepository
import javax.inject.Inject

class UpdatesRepositoryImpl @Inject constructor(private val updatesDao: UpdatesDao) :
    UpdatesRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun subscribeAllUpdates(): Flow<Map<String, List<Update>>> {
        return updatesDao.subscribeUpdates().mapLatest { updates ->
            updates.groupBy { update -> convertLongToTime(update.date, "yyyy/MM/dd") }
        }
    }

    override suspend fun insertUpdates(update: List<Update>) {
        return updatesDao.insertUpdates(update)
    }

    override suspend fun insertUpdate(update: Update) {
        return updatesDao.insertUpdate(update)
    }
}