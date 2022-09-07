package ireader.domain.use_cases.updates

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import ireader.common.data.repository.UpdatesRepository
import ireader.common.models.entities.Update
import ireader.common.models.entities.UpdateWithInfo
import org.koin.core.annotation.Factory

data class UpdateUseCases(
    val subscribeUpdates: SubscribeUpdates,
    val deleteUpdates: DeleteUpdates,
    val deleteAllUpdates: DeleteAllUpdates,
)
@Factory
class SubscribeUpdates(private val updatesRepository: UpdatesRepository) {
    suspend operator fun invoke(): Flow<Map<LocalDate, List<UpdateWithInfo>>> {
        return updatesRepository.subscribeAllUpdates()
    }
}
@Factory
class DeleteUpdates(private val updatesRepository: UpdatesRepository) {
    suspend operator fun invoke(updates: List<Update>) {
        return updatesRepository.deleteUpdates(updates)
    }
}
@Factory
class DeleteAllUpdates(private val updatesRepository: UpdatesRepository) {
    suspend operator fun invoke() {
        return updatesRepository.deleteAllUpdates()
    }
}
