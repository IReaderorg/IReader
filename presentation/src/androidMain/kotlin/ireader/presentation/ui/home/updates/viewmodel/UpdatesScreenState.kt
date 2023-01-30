package ireader.presentation.ui.home.updates.viewmodel

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import ireader.domain.models.entities.UpdatesWithRelations
import kotlinx.datetime.LocalDateTime
import org.koin.core.annotation.Single

@Single
open class UpdateStateImpl: UpdateState {
    override var isLoading: Boolean by mutableStateOf(false)
    override val isEmpty: Boolean by derivedStateOf { updates.isEmpty() }
    override var updates: Map<LocalDateTime, List<UpdatesWithRelations>> by mutableStateOf(emptyMap())
    override var downloadedChapters: List<Long> by mutableStateOf(emptyList<Long>())
    override var selection: SnapshotStateList<Long> = mutableStateListOf()
    override val hasSelection: Boolean by derivedStateOf { selection.isNotEmpty() }
}

interface UpdateState {
    val isLoading: Boolean
    val isEmpty: Boolean
    var updates: Map<LocalDateTime, List<UpdatesWithRelations>>
    var downloadedChapters: List<Long>
    var selection: SnapshotStateList<Long>
    val hasSelection: Boolean
}
