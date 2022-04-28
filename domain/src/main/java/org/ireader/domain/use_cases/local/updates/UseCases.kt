package org.ireader.domain.use_cases.local.updates

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Update
import org.ireader.common_models.entities.UpdateWithInfo
import org.ireader.common_extensions.withIOContext
import javax.inject.Inject

class SubscribeUpdates @Inject constructor(private val updatesRepository: org.ireader.common_data.repository.UpdatesRepository) {
    operator fun invoke(): Flow<Map<String, List<UpdateWithInfo>>> {
        return updatesRepository.subscribeAllUpdates()
    }
}

class InsertUpdatesUseCase @Inject constructor(private val updatesRepository: org.ireader.common_data.repository.UpdatesRepository) {
    suspend operator fun invoke(updates: List<Update>) {
        return org.ireader.common_extensions.withIOContext {
            return@withIOContext updatesRepository.insertUpdates(updates)
        }
    }
}

class InsertUpdateUseCase @Inject constructor(private val updatesRepository: org.ireader.common_data.repository.UpdatesRepository) {
    suspend operator fun invoke(updates: Update) {
        return org.ireader.common_extensions.withIOContext {
            return@withIOContext updatesRepository.insertUpdate(updates)
        }
    }
}