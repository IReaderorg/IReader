package org.ireader.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import org.ireader.common_extensions.convertLongToTime
import org.ireader.common_models.entities.Update
import org.ireader.data.local.dao.UpdatesDao
import javax.inject.Inject

class UpdatesRepositoryImpl @Inject constructor(private val updatesDao: UpdatesDao) :
    org.ireader.common_data.repository.UpdatesRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun subscribeAllUpdates(): Flow<Map<String, List<org.ireader.common_models.entities.UpdateWithInfo>>> {
        return updatesDao.subscribeUpdates().mapLatest { updates ->
            updates.groupBy { update ->
                convertLongToTime(
                    update.date,
                    "yyyy/MM/dd"
                )
            }
        }.distinctUntilChanged()
    }

    override suspend fun insertUpdates(update: List<org.ireader.common_models.entities.Update>) {
        updatesDao.insert(update)
    }

    override suspend fun insertUpdate(update: org.ireader.common_models.entities.Update) {
        updatesDao.insert(update)
    }

    override suspend fun deleteUpdate(update: Update) {
        updatesDao.delete(update)
    }

    override suspend fun deleteUpdates(update: List<Update>) {
        updatesDao.delete(update)
    }

    override suspend fun deleteAllUpdates() {
        updatesDao.deleteAllUpdates()
    }
}
