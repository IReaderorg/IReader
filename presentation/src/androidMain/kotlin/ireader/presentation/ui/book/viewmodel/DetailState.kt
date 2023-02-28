package ireader.presentation.ui.book.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.CatalogLocal
import ireader.core.source.HttpSource
import ireader.core.source.Source
import ireader.core.source.model.Command



open class DetailStateImpl : DetailState {
    override var catalogSource by mutableStateOf<CatalogLocal?>(null)
    override val source by derivedStateOf { catalogSource?.source }

    override var chapterMode by mutableStateOf<Boolean>(true)
    override var inLibraryLoading by mutableStateOf<Boolean>(false)
    override var detailIsLoading by mutableStateOf<Boolean>(false)
    override var showDialog by mutableStateOf<Boolean>(false)
    override var expandedSummary by mutableStateOf(false)
    override var isAsc by mutableStateOf(false)
    override var modifiedCommands: List<Command<*>> by mutableStateOf(emptyList<Command<*>>())

    override var commands: State<List<Command<*>>> = derivedStateOf { catalogSource?.source.let { source -> if (source is HttpSource) source.getCommands() else emptyList() } }
}

interface DetailState {
    var chapterMode: Boolean
    var catalogSource: CatalogLocal?
    val source: Source?
    var inLibraryLoading: Boolean
    var detailIsLoading: Boolean
    var expandedSummary: Boolean
    var modifiedCommands: List<Command<*>>
    var commands: State<List<Command<*>>>
    var isAsc: Boolean
    var showDialog : Boolean

}
