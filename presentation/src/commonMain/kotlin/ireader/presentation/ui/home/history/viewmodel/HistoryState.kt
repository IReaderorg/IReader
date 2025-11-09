package ireader.presentation.ui.home.history.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.HistoryWithRelations
import kotlinx.datetime.LocalDateTime


interface HistoryState {
    val isLoading: Boolean
    val isEmpty: Boolean
    var histories: Map<Long, List<HistoryWithRelations>>

    var searchMode: Boolean
    var searchQuery: String
    var groupByNovel: Boolean
    var dateFilter: DateFilter?
    
    // Add a force refresh signal to trigger UI updates
    var refreshTrigger: Int
}

enum class DateFilter {
    TODAY, YESTERDAY, PAST_7_DAYS
}

class HistoryStateImpl : HistoryState {
    override var histories: Map<Long, List<HistoryWithRelations>> by mutableStateOf(emptyMap())
    override var isLoading: Boolean by mutableStateOf(false)
    override val isEmpty: Boolean by  mutableStateOf(false)
    override var searchMode by mutableStateOf<Boolean>(false)
    override var searchQuery by mutableStateOf<String>("")
    override var groupByNovel by mutableStateOf<Boolean>(false)
    override var dateFilter by mutableStateOf<DateFilter?>(null)
    override var refreshTrigger by mutableStateOf(0)
}
