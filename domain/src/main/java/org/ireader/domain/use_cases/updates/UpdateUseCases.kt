package org.ireader.domain.use_cases.updates

import kotlinx.coroutines.flow.Flow
import org.ireader.common_data.repository.UpdatesRepository
import org.ireader.common_models.entities.Update
import org.ireader.common_models.entities.UpdateWithInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
data class UpdateUseCases @Inject constructor(
    val subscribeUpdates: SubscribeUpdates,
    val deleteUpdates: DeleteUpdates,
    val deleteAllUpdates: DeleteAllUpdates,
)

class SubscribeUpdates @Inject constructor(private val updatesRepository: UpdatesRepository) {
    suspend operator fun invoke(): Flow<Map<String, List<UpdateWithInfo>>> {
        return updatesRepository.subscribeAllUpdates()
    }
}

class DeleteUpdates @Inject constructor(private val updatesRepository: UpdatesRepository) {
    suspend operator fun invoke(updates: List<Update>) {
        return updatesRepository.deleteUpdates(updates)
    }
}

class DeleteAllUpdates @Inject constructor(private val updatesRepository: UpdatesRepository) {
    suspend operator fun invoke() {
        return updatesRepository.deleteAllUpdates()
    }
}
