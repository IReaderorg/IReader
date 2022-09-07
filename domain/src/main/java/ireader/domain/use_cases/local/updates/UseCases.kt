package ireader.domain.use_cases.local.updates

import ireader.common.data.repository.UpdatesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import ireader.common.models.entities.Update
import ireader.common.models.entities.UpdateWithInfo
import org.koin.core.annotation.Factory

@Factory
class SubscribeUpdates(private val updatesRepository: UpdatesRepository) {
    operator fun invoke(): Flow<Map<LocalDate, List<UpdateWithInfo>>> {
        return updatesRepository.subscribeAllUpdates()
    }
}
@Factory
class InsertUpdatesUseCase(private val updatesRepository: UpdatesRepository) {
    suspend operator fun invoke(updates: List<Update>): List<Long> {
        return ireader.common.extensions.withIOContext {
            return@withIOContext updatesRepository.insertUpdates(updates)
        }
    }
}

class InsertUpdateUseCase(private val updatesRepository: UpdatesRepository) {
    suspend operator fun invoke(updates: Update): Long {
        return ireader.common.extensions.withIOContext {
            return@withIOContext updatesRepository.insertUpdate(updates)
        }
    }
}
