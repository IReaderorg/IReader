package ireader.data.repository

import ireader.common.data.repository.UpdatesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import ireader.common.models.entities.Update
import ireader.data.local.dao.UpdatesDao


class UpdatesRepositoryImpl(private val updatesDao: UpdatesDao) :
    UpdatesRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun subscribeAllUpdates(): Flow<Map<LocalDate, List<ireader.common.models.entities.UpdateWithInfo>>> {
        return updatesDao.subscribeUpdates().mapLatest { updates ->
            updates.groupBy { update ->
                update.date.toLocalDate()
            }
        }.distinctUntilChanged()
    }

    override suspend fun insertUpdates(update: List<ireader.common.models.entities.Update>): List<Long> {
        return updatesDao.insertOrUpdate(update)
    }

    override suspend fun insertUpdate(update: ireader.common.models.entities.Update): Long {
        return updatesDao.insertOrUpdate(update)
    }

    override suspend fun deleteUpdate(update: Update) {
        return updatesDao.delete(update)
    }

    override suspend fun deleteUpdates(update: List<Update>) {
        return updatesDao.delete(update)
    }

    override suspend fun deleteAllUpdates() {
        updatesDao.deleteAllUpdates()
    }
}
