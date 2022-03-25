package org.ireader.presentation.feature_updates.viewmodel


import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.ireader.domain.models.entities.Update
import javax.inject.Inject


open class UpdateStateImpl @Inject constructor() : UpdateState {
    override var isLoading: Boolean by mutableStateOf(false)
    override val isEmpty: Boolean by derivedStateOf { updates.isEmpty() }
    override var updates: Map<String, List<Update>> by mutableStateOf(emptyMap())
    override var selection: SnapshotStateList<Long> = mutableStateListOf()
    override val hasSelection: Boolean by derivedStateOf { selection.isNotEmpty() }
}


interface UpdateState {
    val isLoading: Boolean
    val isEmpty: Boolean
    var updates: Map<String, List<Update>>
    var selection: SnapshotStateList<Long>
    val hasSelection: Boolean

}

