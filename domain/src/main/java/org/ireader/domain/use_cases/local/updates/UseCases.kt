package org.ireader.domain.use_cases.local.updates

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.ireader.common_models.entities.Update
import org.ireader.common_models.entities.UpdateWithInfo
import javax.inject.Inject

class SubscribeUpdates @Inject constructor(private val updatesRepository: org.ireader.common_data.repository.UpdatesRepository) {
    operator fun invoke(): Flow<Map<LocalDate, List<UpdateWithInfo>>> {
        return updatesRepository.subscribeAllUpdates()
    }
}

class InsertUpdatesUseCase @Inject constructor(private val updatesRepository: org.ireader.common_data.repository.UpdatesRepository) {
    suspend operator fun invoke(updates: List<Update>): List<Long> {
        return org.ireader.common_extensions.withIOContext {
            return@withIOContext updatesRepository.insertUpdates(updates)
        }
    }
}

class InsertUpdateUseCase @Inject constructor(private val updatesRepository: org.ireader.common_data.repository.UpdatesRepository) {
    suspend operator fun invoke(updates: Update): Long {
        return org.ireader.common_extensions.withIOContext {
            return@withIOContext updatesRepository.insertUpdate(updates)
        }
    }
}
