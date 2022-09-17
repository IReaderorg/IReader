package ireader.ui.home.history.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single

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
