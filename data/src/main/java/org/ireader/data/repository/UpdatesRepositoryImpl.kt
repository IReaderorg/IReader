package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.data.local.dao.UpdatesDao
import org.ireader.domain.models.entities.Update
import org.ireader.domain.repository.UpdatesRepository
import javax.inject.Inject

class UpdatesRepositoryImpl @Inject constructor(private val updatesDao: UpdatesDao) :
    UpdatesRepository {
    override fun subscribeAllUpdates(): Flow<List<Update>> {
        return updatesDao.subscribeUpdates()
    }

    override suspend fun insertUpdates(update: List<Update>) {
        return updatesDao.insertUpdates(update)
    }

    override suspend fun insertUpdate(update: Update) {
        return updatesDao.insertUpdate(update)
    }
}