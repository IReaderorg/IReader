package ireader.ui.home.history.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.PagingData
import kotlinx.datetime.LocalDate
import ireader.common.models.entities.HistoryWithRelations
import org.koin.core.annotation.Single
import java.util.*

interface HistoryState {
    val isLoading: Boolean
    val isEmpty: Boolean

    var searchMode: Boolean
    var searchQuery: String

}

    @Single()
class HistoryStateImpl : HistoryState {
    override var isLoading: Boolean by mutableStateOf(false)
    override val isEmpty: Boolean by  mutableStateOf(false)
    override var searchMode by mutableStateOf<Boolean>(false)
    override var searchQuery by mutableStateOf<String>("")
}
