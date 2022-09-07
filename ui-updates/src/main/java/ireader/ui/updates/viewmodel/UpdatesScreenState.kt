package ireader.ui.updates.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.datetime.LocalDate
import ireader.common.models.entities.UpdateWithInfo
import org.koin.core.annotation.Single

@Single
open class UpdateStateImpl: UpdateState {
    override var isLoading: Boolean by mutableStateOf(false)
    override val isEmpty: Boolean by derivedStateOf { updates.isEmpty() }
    override var updates: Map<LocalDate, List<UpdateWithInfo>> by mutableStateOf(emptyMap())
    override var selection: SnapshotStateList<Long> = mutableStateListOf()
    override val hasSelection: Boolean by derivedStateOf { selection.isNotEmpty() }
}

interface UpdateState {
    val isLoading: Boolean
    val isEmpty: Boolean
    var updates: Map<LocalDate, List<UpdateWithInfo>>
    var selection: SnapshotStateList<Long>
    val hasSelection: Boolean
}
