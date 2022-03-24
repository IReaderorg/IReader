package org.ireader.domain.use_cases.local.updates

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Update
import org.ireader.domain.repository.UpdatesRepository
import javax.inject.Inject

class SubscribeUpdates @Inject constructor(private val updatesRepository: UpdatesRepository) {
    operator fun invoke(): Flow<List<Update>> {
        return updatesRepository.subscribeAllUpdates()
    }
}

class InsertUpdatesUseCase @Inject constructor(private val updatesRepository: UpdatesRepository) {
    suspend operator fun invoke(updates: List<Update>) {
        return updatesRepository.insertUpdates(updates)
    }
}

class InsertUpdateUseCase @Inject constructor(private val updatesRepository: UpdatesRepository) {
    suspend operator fun invoke(updates: Update) {
        return updatesRepository.insertUpdate(updates)
    }
}