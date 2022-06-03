package org.ireader.updates.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.datetime.LocalDate
import org.ireader.common_models.entities.UpdateWithInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class UpdateStateImpl @Inject constructor() : UpdateState {
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
