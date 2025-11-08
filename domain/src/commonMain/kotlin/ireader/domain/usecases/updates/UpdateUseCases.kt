package ireader.domain.usecases.updates

import ireader.domain.data.repository.UpdatesRepository
import ireader.domain.models.entities.UpdatesWithRelations
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.utils.extensions.toLocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import kotlin.time.ExperimentalTime


data class UpdateUseCases(
    val subscribeUpdates: SubscribeUpdates,
    val deleteAllUpdates: DeleteAllUpdates,
)

class SubscribeUpdates(private val updatesRepository: UpdatesRepository) {
    operator fun invoke(after: Long): Flow<Map<LocalDateTime, List<UpdatesWithRelations>>> {
        return updatesRepository.subscribeAllUpdates(after).map { list ->
            list.groupBy { it.dateFetch.toLocalDate() }
        }
    }
}


class DeleteAllUpdates(private val prefs: UiPreferences) {
    @OptIn(ExperimentalTime::class)
    operator fun invoke() {
        return prefs.showUpdatesAfter().set(kotlin.time.Clock.System.now().toEpochMilliseconds())
    }
}


