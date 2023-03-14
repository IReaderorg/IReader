package ireader.presentation.ui.home.history.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.HistoryWithRelations
import kotlinx.datetime.LocalDateTime


interface HistoryState {
    val isLoading: Boolean
    val isEmpty: Boolean
    var histories: Map<LocalDateTime, List<HistoryWithRelations>>

    var searchMode: Boolean
    var searchQuery: String

}

class HistoryStateImpl : HistoryState {
    override var histories: Map<LocalDateTime, List<HistoryWithRelations>> by mutableStateOf(emptyMap())
    override var isLoading: Boolean by mutableStateOf(false)
    override val isEmpty: Boolean by  mutableStateOf(false)
    override var searchMode by mutableStateOf<Boolean>(false)
    override var searchQuery by mutableStateOf<String>("")
}
