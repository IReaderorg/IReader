package ireader.presentation.ui.home.updates.viewmodel

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import ireader.domain.models.entities.UpdatesWithRelations
import kotlinx.datetime.LocalDateTime



open class UpdateStateImpl: UpdateState {
    override var isLoading: Boolean by mutableStateOf(false)
    override var isRefreshing: Boolean by mutableStateOf(false)
    override var updateProgress: UpdateProgress? by mutableStateOf(null)
    override var selectedCategoryId: Long? by mutableStateOf(null)
    override val isEmpty: Boolean by derivedStateOf { updates.isEmpty() }
    override var updates: Map<LocalDateTime, List<UpdatesWithRelations>> by mutableStateOf(emptyMap())
    override var updateHistory: List<ireader.domain.models.entities.UpdateHistory> by mutableStateOf(emptyList())
    override var downloadedChapters: List<Long> by mutableStateOf(emptyList<Long>())
    override var selection: SnapshotStateList<Long> = mutableStateListOf()
    override val hasSelection: Boolean by derivedStateOf { selection.isNotEmpty() }
}

interface UpdateState {
    val isLoading: Boolean
    var isRefreshing: Boolean
    val updateProgress: UpdateProgress?
    var selectedCategoryId: Long?
    val isEmpty: Boolean
    var updates: Map<LocalDateTime, List<UpdatesWithRelations>>
    var updateHistory: List<ireader.domain.models.entities.UpdateHistory>
    var downloadedChapters: List<Long>
    var selection: SnapshotStateList<Long>
    val hasSelection: Boolean
}

data class UpdateProgress(
    val currentBook: String,
    val currentIndex: Int,
    val totalBooks: Int,
    val estimatedTimeRemaining: Long? = null
)
