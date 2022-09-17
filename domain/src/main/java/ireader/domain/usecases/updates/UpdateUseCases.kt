package ireader.domain.usecases.updates

import ireader.common.models.entities.UpdatesWithRelations
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.data.repository.UpdatesRepository
import ireader.domain.utils.extensions.toLocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import org.koin.core.annotation.Factory

data class UpdateUseCases(
    val subscribeUpdates: SubscribeUpdates,
    val deleteAllUpdates: DeleteAllUpdates,
)
@Factory
class SubscribeUpdates(private val updatesRepository: UpdatesRepository) {
    operator fun invoke(after: Long): Flow<Map<LocalDateTime, List<UpdatesWithRelations>>> {
        return updatesRepository.subscribeAllUpdates(after).map { list ->
            list.groupBy { it.dateFetch.toLocalDate() }
        }
    }
}

@Factory
class DeleteAllUpdates(private val prefs: UiPreferences) {
    operator fun invoke() {
        return prefs.showUpdatesAfter().set(Clock.System.now().toEpochMilliseconds())
    }
}


